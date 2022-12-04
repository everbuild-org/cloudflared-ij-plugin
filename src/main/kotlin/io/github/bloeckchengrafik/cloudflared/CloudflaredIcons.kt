package io.github.bloeckchengrafik.cloudflared

import com.intellij.openapi.util.IconLoader

interface CloudflaredIcons {
    companion object {
        val ENABLED = IconLoader.getIcon("active.svg", CloudflaredIcons::class.java)
        val DISABLED = IconLoader.getIcon("inactive.svg", CloudflaredIcons::class.java)
    }
}