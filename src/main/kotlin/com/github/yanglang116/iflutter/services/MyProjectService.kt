package com.github.yanglang116.iflutter.services

import com.intellij.openapi.project.Project
import com.github.yanglang116.iflutter.MyBundle

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
