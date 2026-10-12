package com.together.daily

data class MemoDraft(val text:String="",val baseline:String="") {
    fun remote(saved:String):MemoDraft = if(text==baseline) MemoDraft(saved,saved) else this
    fun confirmed(submitted:String):MemoDraft = copy(text=if(text==submitted) submitted.trim() else text,baseline=submitted.trim())
}
