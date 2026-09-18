-dontwarn okhttp3.**
-dontwarn okio.**

# Release 构建移除所有 android.util.Log 调用，防止邮箱地址、验证码、接口数据泄露到 logcat
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
    public static int e(...);
    public static int wtf(...);
}
