# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# --- WorkManager ------------------------------------------------------------
# The default WorkerFactory instantiates workers via Class.forName + reflection
# on the (Context, WorkerParameters) constructor. Without this keep, every
# scheduled reminder (BriefingWorker, LoggingReminderWorker, MissionReminderWorker,
# and any future one) silently fails at runtime in a minified build — the app
# still installs and runs, but no reminder ever fires and there's no crash log
# pointing at why.
-keep public class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# --- Room ---------------------------------------------------------------
# Entities and DAOs are wired at compile time via KSP-generated code, not
# reflection, but keeping the entity fields protects TypeConverters (DateTypeConverter)
# and the schema from being renamed inconsistently across incremental builds.
-keep class com.example.personallevelingsystem.model.** { *; }
-keep class com.example.personallevelingsystem.data.*Dao { *; }

# --- kotlinx.serialization ---------------------------------------------------
# decodeFromString<FoodAnalysisResponse>() resolves its serializer at compile
# time (no reflection), so this is defense-in-depth rather than a hard
# requirement — but it's cheap and guards against a future field rename.
-keepattributes *Annotation*, InnerClasses
-keep,includedescriptorclasses class com.example.personallevelingsystem.model.FoodAnalysisResponse { *; }
-keep,includedescriptorclasses class com.example.personallevelingsystem.model.Ingredient { *; }
-keepclassmembers class com.example.personallevelingsystem.model.**$Companion {
    kotlinx.serialization.KSerializer serializer(...);
}

# --- Google API Client / Calendar (Planning screen) --------------------------
# google-http-client-gson parses Calendar API responses via Gson reflection on
# @Key-annotated fields of GenericJson subclasses. Without this, event titles,
# dates, etc. silently come back null/empty in a release build.
-keepclassmembers class * extends com.google.api.client.json.GenericJson {
    @com.google.api.client.util.Key <fields>;
}
-keep class com.google.api.services.calendar.model.** { *; }
-keep class com.google.api.client.** { *; }
-dontwarn com.google.api.client.**
-keepattributes Signature,RuntimeVisibleAnnotations,AnnotationDefault

# --- Gemini SDK (AI food scanner) --------------------------------------------
-keep class com.google.ai.client.generativeai.** { *; }
-dontwarn com.google.ai.client.generativeai.**

# --- google-http-client's optional Apache HttpClient transport ---------------
# Never instantiated on Android (the app uses NetHttpTransport, see
# PlanningScreen), but the class file references JVM-only APIs
# (javax.naming.*, org.ietf.jgss.*, i.e. LDAP/Kerberos) that don't exist on
# Android and would otherwise fail the R8 "missing classes" check.
-dontwarn javax.naming.InvalidNameException
-dontwarn javax.naming.NamingException
-dontwarn javax.naming.directory.Attribute
-dontwarn javax.naming.directory.Attributes
-dontwarn javax.naming.ldap.LdapName
-dontwarn javax.naming.ldap.Rdn
-dontwarn org.ietf.jgss.GSSContext
-dontwarn org.ietf.jgss.GSSCredential
-dontwarn org.ietf.jgss.GSSException
-dontwarn org.ietf.jgss.GSSManager
-dontwarn org.ietf.jgss.GSSName
-dontwarn org.ietf.jgss.Oid