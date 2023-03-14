package com.example.myapplication.project_activity

data class ProjectsCardClass(
    var pName : String ?= null,
    var pCreator : String ?= null,
    var userID : ArrayList<String> ?= null,
    var projectUid : String ?= null,
    var pdueDate : String ?= null
)