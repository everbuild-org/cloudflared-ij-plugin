package io.github.bloeckchengrafik.cloudflared

class CloudflaredSingleton private constructor() {
    private var isRunning = false
    private var process: Process? = null
    private fun updateStatus() {
        // If isRunning is true, start the cloudflared process if it is not running
        // If isRunning is false, stop the cloudflared process if it is running
        if (isRunning) {
            if (process == null) {
                try {
                    println("Starting cloudflared")
                    // Execute the cloudflared process in the background but bound to the current process
                    // So that it stops when the current process stops
                    process = ProcessBuilder("cloudflared", "tunnel", "run").start()

                    process!!.onExit().thenAccept {
                        // Find out why the process exited
                        val exitCode = it.exitValue()
                        if (exitCode == 0) {
                            println("Cloudflared process exited normally")
                        } else {
                            println("Cloudflared process exited with code $exitCode")
                        }

                        process = null
                    }

                    println("Cloudflared started, status: " + process!!.isAlive)
                } catch (e: Exception) {
                    println("Error starting cloudflared: " + e.message)
                    e.printStackTrace()
                }
            }
        } else {
            if (process != null) {
                println("Stopping cloudflared")
                process!!.destroy()
                process = null
            }
        }
    }

    fun isRunning(): Boolean {
        return isRunning
    }

    fun setRunning(running: Boolean) {
        isRunning = running
        updateStatus()
    }

    fun getStatus(): String {
        val status = if (isRunning) "running" else "stopped"
        val processInfo = if (process != null) " (process: " + process!!.isAlive + ")" else "(dead)"
        val pidInfo = if (process != null) " (pid: " + process!!.pid() + ")" else ""
        return status + processInfo + pidInfo
    }

    fun toggleRunning() {
        isRunning = !isRunning
        updateStatus()
    }

    companion object {
        private var instance: CloudflaredSingleton? = null
        fun getInstance(): CloudflaredSingleton {
            if (instance == null) {
                instance = CloudflaredSingleton()
            }
            return instance!!
        }
    }
}