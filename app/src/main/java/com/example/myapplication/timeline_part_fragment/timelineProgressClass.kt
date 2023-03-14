package com.example.myapplication.timeline_part_fragment

data class timelineProgressClass (
    var progressTitle : String ?= null,
    var colorCode : String ?= null,
    var context : ArrayList<String> ?= null,
    var contextDecs : ArrayList<String> ?= null,
){}