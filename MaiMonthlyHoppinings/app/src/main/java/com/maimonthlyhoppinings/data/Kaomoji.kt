package com.maimonthlyhoppinings.data

data class KaomojiGroup(
    val label: String,
    val faces: List<String>,
)

/**
 * Popular faces from the usual kaomoji copy libraries
 * (https://kaomoji.ru/en/), grouped to the app's default moods.
 */
object Kaomoji {
    val groups: List<KaomojiGroup> = listOf(
        KaomojiGroup(
            label = "Period",
            faces = listOf(
                "[(－－)]..zzZ",
                "(－_－) zzZ",
                "(∪｡∪)｡｡｡zzZ",
                "(－ω－) zzZ",
                "(￣o￣) zzZZzzZZ",
                "(( _ _ ))..zzzZZ",
                "(￣ρ￣)..zzZZ",
                "(－.－)...zzz",
                "(︸ ︸*) Z z z",
                "(x . x) ~~zzZ",
                "(￣ヘ￣)",
                "(￣ⵁ￣)",
                "(--_--)",
                "(；￣Д￣)",
                "(￣ ￣|||)",
                "(＃￣ω￣)",
                "(⇀⳨⇁‶)",
                "(눈_눈)",
                "(´-ω-`)",
                "(u_u)",
                "(=_=)",
                "(。-ω-)zzz",
                "(๑-﹏-๑)",
                "(ᵕ—ᴗ—)",
                "(｡-_-｡)",
            ),
        ),
        KaomojiGroup(
            label = "Anxious",
            faces = listOf(
                "(´･ω･`)",
                "(・・;)",
                "(⊙_⊙)",
                "(/ω＼)",
                "(´-﹏-`；)",
                "(ノωヽ)",
                "(／。＼)",
                "(ﾉ_ヽ)",
                "(・人・)",
                "＼(〇_ｏ)／",
                "(/_＼)",
                "〜(＞＜)〜",
                "Σ(°△°|||)︴",
                "(((＞＜)))",
                "(°□°;)",
                "Σ(°ロ°)!!!",
                "(￣ω￣;)",
                "(-_-;)・・・",
                "(・_・;)",
                "(＠_＠)",
                "(・・ ) ?",
                "(•ิ_•ิ)?",
                "(ーー;)",
                "(⊙.⊙)?",
                "(￣～￣;)",
            ),
        ),
        KaomojiGroup(
            label = "Happy",
            faces = listOf(
                "(* ^ ω ^)",
                "(´ ∀ ` *)",
                "٩(◕‿◕｡)۶",
                "(o^▽^o)",
                "(⌒▽⌒)☆",
                "ヽ(・∀・)ﾉ",
                "(´｡• ω •｡`)",
                "(o･ω･o)",
                "(^人^)",
                "(o´▽`o)",
                "(*´▽`*)",
                "(≧◡≦)",
                "(＾▽＾)",
                "(◕‿◕)",
                "(*^‿^*)",
                "＼(≧▽≦)／",
                "(*°▽°*)",
                "(´｡• ᵕ •｡`)",
                "＼(＾▽＾)／",
                "(ﾉ◕ヮ◕)ﾉ*:･ﾟ✧",
                "(„• ֊ •„)",
                "(.❛ ᴗ ❛.)",
                "(ᵔ◡ᵔ)",
                "╰(▔∀▔)╯",
                "(≧▽≦)",
            ),
        ),
        KaomojiGroup(
            label = "Sad",
            faces = listOf(
                "(ノ_<。)",
                "(μ_μ)",
                "(ﾉД`)",
                "o(TヘTo)",
                "( ; ω ; )",
                "(｡╯︵╰｡)",
                "( ╥ω╥ )",
                "(╥_╥)",
                "(╥﹏╥)",
                "(つω`｡)",
                "(｡T ω T｡)",
                "(T_T)",
                "(っ˘̩╭╮˘̩)っ",
                "o(〒﹏〒)o",
                "(｡•́︿•̀｡)",
                "(ಥ﹏ಥ)",
                "(╯︵╰,)",
                "(ノ_<、)",
                "｡ﾟ(｡ﾉωヽ｡)ﾟ｡",
                "(╯_╰)",
                "(个_个)",
                "(ﾉω･､)",
                "(ಡ‸ಡ)",
                "｡ﾟ･ (>﹏<) ･ﾟ｡",
                "(-ω-、)",
            ),
        ),
        KaomojiGroup(
            label = "Cramps",
            faces = listOf(
                "~(>_<~)",
                "☆⌒(> _ <)",
                "☆⌒(>。<)",
                "(☆_@)",
                "(×_×)",
                "(x_x)",
                "(×_×)⌒☆",
                "(x_x)⌒☆",
                "(×﹏×)",
                "☆(＃××)",
                "(＋_＋)",
                "[ ± _ ± ]",
                "٩(× ×)۶",
                "_:(´ཀ`」 ∠):_",
                "(ﾒ﹏ﾒ)",
                "(>_<)",
                "(＞﹏＜)",
                "(＃＞＜)",
                "(＞ｍ＜)",
                "(〃＞＿＜;〃)",
                "o(>< )o",
                "(」＞＜)」",
                "(ᗒᗣᗕ)՞",
                "(×o×)",
                "(+_+)",
            ),
        ),
    )

    val all: Set<String> = groups.flatMap { it.faces }.toSet()

    fun groupForLabel(label: String): KaomojiGroup {
        return groups.firstOrNull { it.label.equals(label, ignoreCase = true) }
            ?: groups.first()
    }

    private const val FACE_HINTS = "()[]{}\\/_^;?~-+*<>|`."

    fun isKaomoji(tag: String): Boolean {
        if (tag in all) return true
        return tag.any { it in FACE_HINTS }
    }
}
