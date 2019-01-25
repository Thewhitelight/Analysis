package cn.libery.analysis.trace

class TraceExtension {

    List<String> classExcludes = new ArrayList<>()
    List<String> packageExcludes = new ArrayList<>()


    def enabled = true
    def packageName
    def logLevel = "d"

    def getEnabled() {
        return enabled
    }

    void setEnabled(enabled) {
        this.enabled = enabled
    }

    def getPackageName() {
        return packageName
    }

    void setPackageName(packageName) {
        this.packageName = packageName
    }

    def getLogLevel() {
        return logLevel
    }

    void setLogLevel(logLevel) {
        this.logLevel = logLevel
    }

    List<String> getClassExcludes() {
        return classExcludes
    }

    void setClassExcludes(List<String> classExcludes) {
        this.classExcludes = classExcludes
    }

    List<String> getPackageExcludes() {
        return packageExcludes
    }

    void setPackageExcludes(List<String> packageExcludes) {
        this.packageExcludes = packageExcludes
    }
}