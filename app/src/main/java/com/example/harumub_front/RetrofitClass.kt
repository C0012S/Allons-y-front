package com.example.harumub_front


data class LoginResult(var id: String, var name: String, var record: Boolean)
//, var password : String, var number : String, var birth : String)

data class MakeRoomResult(var roomCode: String)

data class EmailResult(var code: String)

data class WatchListResult(var title: String, var poster: String)

// emotion > array / highlight > array / rating: Int > Float
data class WatchResult(var title: String, var poster : String, var genres: String, var concentration: String,
                       var emotion_array : Emotion, var highlight_array: Highlight,
                       var rating : Float, var comment : String, var sleepingCount: Int)

data class Emotion(val HAPPY : Int, val SAD : Int, val ANGRY : Int, val CONFUSED : Int,
                   val DISGUSTED : Int, val SURPRISED : Int, val FEAR : Int)

data class Highlight(val time: Int, val emotion: String, val emotion_diff: Int)

data class Recommend2Result(var userId: String, var title: String, var poster: String)
