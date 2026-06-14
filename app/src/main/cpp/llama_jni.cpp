#include <jni.h>
#include <string>
#include <vector>
#include <android/log.h>
#include "llama.h"

#define TAG "llama-android"
#define LOG_I(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOG_E(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

static void log_callback(enum ggml_log_level level, const char * text, void * user_data) {
    if (level == GGML_LOG_LEVEL_ERROR) {
        LOG_E("%s", text);
    } else {
        LOG_I("%s", text);
    }
}

extern "C" {

JNIEXPORT jlong JNICALL
Java_com_androidagent_data_llm_LlamaCppEngine_nativeLoadModel(
    JNIEnv *env, jobject thiz, jstring path, jint gpu_layers) {

    llama_log_set(log_callback, nullptr);
    llama_backend_init();

    const char *model_path = env->GetStringUTFChars(path, nullptr);

    auto params = llama_model_default_params();
    params.n_gpu_layers = gpu_layers;

    llama_model *model = llama_model_load_from_file(model_path, params);
    env->ReleaseStringUTFChars(path, model_path);

    if (!model) {
        LOG_E("Failed to load model");
        return 0;
    }

    LOG_I("Model loaded successfully");
    return reinterpret_cast<jlong>(model);
}

JNIEXPORT jlong JNICALL
Java_com_androidagent_data_llm_LlamaCppEngine_nativeCreateContext(
    JNIEnv *env, jobject thiz, jlong model_ptr, jint context_size, jint threads) {

    auto *model = reinterpret_cast<llama_model *>(model_ptr);
    auto ctx_params = llama_context_default_params();
    ctx_params.n_ctx = context_size;
    ctx_params.n_threads = threads;
    ctx_params.n_threads_batch = threads;

    llama_context *ctx = llama_init_from_model(model, ctx_params);
    if (!ctx) {
        LOG_E("Failed to create context");
        return 0;
    }

    return reinterpret_cast<jlong>(ctx);
}

JNIEXPORT void JNICALL
Java_com_androidagent_data_llm_LlamaCppEngine_nativeFreeModel(
    JNIEnv *env, jobject thiz, jlong model_ptr) {
    auto *model = reinterpret_cast<llama_model *>(model_ptr);
    if (model) llama_model_free(model);
}

JNIEXPORT void JNICALL
Java_com_androidagent_data_llm_LlamaCppEngine_nativeFreeContext(
    JNIEnv *env, jobject thiz, jlong ctx_ptr) {
    auto *ctx = reinterpret_cast<llama_context *>(ctx_ptr);
    if (ctx) llama_free(ctx);
}

JNIEXPORT void JNICALL
Java_com_androidagent_data_llm_LlamaCppEngine_nativeClearContext(
    JNIEnv *env, jobject thiz, jlong ctx_ptr) {
    auto *ctx = reinterpret_cast<llama_context *>(ctx_ptr);
    if (ctx) llama_kv_cache_clear(ctx);
}

JNIEXPORT jstring JNICALL
Java_com_androidagent_data_llm_LlamaCppEngine_nativeGenerate(
    JNIEnv *env, jobject thiz, jlong ctx_ptr, jstring prompt,
    jint max_tokens, jfloat temperature, jfloat top_p, jint top_k,
    jfloat repeat_penalty, jobjectArray stop_sequences, jobject callback) {

    auto *ctx = reinterpret_cast<llama_context *>(ctx_ptr);
    const llama_model *model = llama_get_model(ctx);
    const llama_vocab *vocab = llama_model_get_vocab(model);

    const char *prompt_cstr = env->GetStringUTFChars(prompt, nullptr);
    std::string prompt_str(prompt_cstr);
    env->ReleaseStringUTFChars(prompt, prompt_cstr);

    int n_prompt_tokens = -llama_tokenize(vocab, prompt_str.c_str(), prompt_str.length(), nullptr, 0, true, true);
    std::vector<llama_token> tokens(n_prompt_tokens);
    llama_tokenize(vocab, prompt_str.c_str(), prompt_str.length(), tokens.data(), tokens.size(), true, true);

    llama_kv_cache_clear(ctx);

    llama_batch batch = llama_batch_init(tokens.size(), 0, 1);
    for (size_t i = 0; i < tokens.size(); i++) {
        llama_batch_add(batch, tokens[i], i, {0}, i == tokens.size() - 1);
    }
    llama_decode(ctx, batch);
    llama_batch_free(batch);

    std::vector<std::string> stop_strs;
    int stop_count = env->GetArrayLength(stop_sequences);
    for (int i = 0; i < stop_count; i++) {
        auto jstr = (jstring)env->GetObjectArrayElement(stop_sequences, i);
        const char *s = env->GetStringUTFChars(jstr, nullptr);
        stop_strs.emplace_back(s);
        env->ReleaseStringUTFChars(jstr, s);
    }

    jclass callbackClass = nullptr;
    jmethodID invokeMethod = nullptr;
    if (callback) {
        callbackClass = env->GetObjectClass(callback);
        invokeMethod = env->GetMethodID(callbackClass, "invoke", "(Ljava/lang/Object;)Ljava/lang/Object;");
    }

    auto *smpl = llama_sampler_chain_init(llama_sampler_chain_default_params());
    llama_sampler_chain_add(smpl, llama_sampler_init_top_k(top_k));
    llama_sampler_chain_add(smpl, llama_sampler_init_top_p(top_p, 1));
    llama_sampler_chain_add(smpl, llama_sampler_init_temp(temperature));
    llama_sampler_chain_add(smpl, llama_sampler_init_dist(0));

    std::string result;
    int n_cur = tokens.size();

    for (int i = 0; i < max_tokens; i++) {
        llama_token new_token = llama_sampler_sample(smpl, ctx, -1);

        if (llama_vocab_is_eog(vocab, new_token)) break;

        char buf[256];
        int n = llama_token_to_piece(vocab, new_token, buf, sizeof(buf), 0, true);
        std::string piece(buf, n);
        result += piece;

        if (callback && invokeMethod) {
            jstring jpiece = env->NewStringUTF(piece.c_str());
            env->CallObjectMethod(callback, invokeMethod, jpiece);
            env->DeleteLocalRef(jpiece);
        }

        bool should_stop = false;
        for (const auto &stop : stop_strs) {
            if (result.length() >= stop.length() &&
                result.substr(result.length() - stop.length()) == stop) {
                result = result.substr(0, result.length() - stop.length());
                should_stop = true;
                break;
            }
        }
        if (should_stop) break;

        llama_batch next_batch = llama_batch_init(1, 0, 1);
        llama_batch_add(next_batch, new_token, n_cur, {0}, true);
        llama_decode(ctx, next_batch);
        llama_batch_free(next_batch);
        n_cur++;
    }

    llama_sampler_free(smpl);
    return env->NewStringUTF(result.c_str());
}

JNIEXPORT jint JNICALL
Java_com_androidagent_data_llm_LlamaCppEngine_nativeTokenCount(
    JNIEnv *env, jobject thiz, jlong ctx_ptr, jstring text) {

    auto *ctx = reinterpret_cast<llama_context *>(ctx_ptr);
    const llama_model *model = llama_get_model(ctx);
    const llama_vocab *vocab = llama_model_get_vocab(model);

    const char *text_cstr = env->GetStringUTFChars(text, nullptr);
    int count = -llama_tokenize(vocab, text_cstr, strlen(text_cstr), nullptr, 0, false, false);
    env->ReleaseStringUTFChars(text, text_cstr);
    return count;
}

}
