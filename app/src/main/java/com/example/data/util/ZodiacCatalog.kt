package com.example.data.util

import com.example.data.model.ZodiacSignInfo

object ZodiacCatalog {
    val signs: List<ZodiacSignInfo> = listOf(
        ZodiacSignInfo(
            sign = "Aries",
            symbol = "♈",
            dates = "Mar 21 – Apr 19",
            element = "Fire",
            dailyHoroscope = "A fierce celestial spark ignites your competitive edge. Step boldly onto the stage; hesitation is your only adversary in the shadows.",
            luckyColor = "#B00020",
            luckyNumber = 9,
            darkPersonality = "The Blood Ram: Relentless passion that burns through all obstacles."
        ),
        ZodiacSignInfo(
            sign = "Taurus",
            symbol = "♉",
            dates = "Apr 20 – May 20",
            element = "Earth",
            dailyHoroscope = "Ancient earth vibrations ground your artistic focus. Let your voice or performance resonate with immovable power.",
            luckyColor = "#C6A36A",
            luckyNumber = 6,
            darkPersonality = "The Minotaur: Unyielding resolve cloaked in gothic majesty."
        ),
        ZodiacSignInfo(
            sign = "Gemini",
            symbol = "♊",
            dates = "May 21 – Jun 20",
            element = "Air",
            dailyHoroscope = "Twin shadows whisper divergent inspirations. Unify your duality to captivate the festival audience with electric charisma.",
            luckyColor = "#E5C38C",
            luckyNumber = 5,
            darkPersonality = "The Dual Phantom: Master of illusions and sharp wit."
        ),
        ZodiacSignInfo(
            sign = "Cancer",
            symbol = "♋",
            dates = "Jun 21 – Jul 22",
            element = "Water",
            dailyHoroscope = "A crimson moon reveals deep emotional resonance in your performance. Tap into the eerie depth of ancient folklore.",
            luckyColor = "#B8B8B8",
            luckyNumber = 2,
            darkPersonality = "The Lunar Spectre: An intuitive mystic guided by tides of destiny."
        ),
        ZodiacSignInfo(
            sign = "Leo",
            symbol = "♌",
            dates = "Jul 23 – Aug 22",
            element = "Fire",
            dailyHoroscope = "The eclipse cannot dim your solar crown. Command the spotlight; all eyes are destined to witness your nocturnal triumph.",
            luckyColor = "#DFBE82",
            luckyNumber = 1,
            darkPersonality = "The Golden Chimera: Born to rule the darkened amphitheater."
        ),
        ZodiacSignInfo(
            sign = "Virgo",
            symbol = "♍",
            dates = "Aug 23 – Sep 22",
            element = "Earth",
            dailyHoroscope = "Every meticulous note and step aligns with cosmic precision. Perfection is not sought; it is commanded under the veil.",
            luckyColor = "#8C734B",
            luckyNumber = 4,
            darkPersonality = "The Alchemist: Transforms obscure chaos into flawless artistry."
        ),
        ZodiacSignInfo(
            sign = "Libra",
            symbol = "♎",
            dates = "Sep 23 – Oct 22",
            element = "Air",
            dailyHoroscope = "The scales tip toward dark elegance. Blend horror and beauty seamlessly to bewitch the judges and crowd.",
            luckyColor = "#B00020",
            luckyNumber = 7,
            darkPersonality = "The Arbiter of Twilight: Harmonizes mortal fear with divine poise."
        ),
        ZodiacSignInfo(
            sign = "Scorpio",
            symbol = "♏",
            dates = "Oct 23 – Nov 21",
            element = "Water",
            dailyHoroscope = "Pluto and Mars reign supreme over CADUCEUS. Your hypnotic presence penetrates the veil; victory is sealed in darkness.",
            luckyColor = "#650014",
            luckyNumber = 8,
            darkPersonality = "The Serpent King: Potent venom and unmatched psychic magnetism."
        ),
        ZodiacSignInfo(
            sign = "Sagittarius",
            symbol = "♐",
            dates = "Nov 22 – Dec 21",
            element = "Fire",
            dailyHoroscope = "Your fiery arrow pierces the obsidian sky. Explore unexpected competitions and push the boundaries of the festival.",
            luckyColor = "#FF2A4B",
            luckyNumber = 3,
            darkPersonality = "The Shadow Archer: Hunter of hidden truths and legendary feats."
        ),
        ZodiacSignInfo(
            sign = "Capricorn",
            symbol = "♑",
            dates = "Dec 22 – Jan 19",
            element = "Earth",
            dailyHoroscope = "Saturn's dark rings build your impenetrable discipline. Endurance will crown you victor while rivals falter.",
            luckyColor = "#171717",
            luckyNumber = 10,
            darkPersonality = "The Abyssal Sovereign: Patient architect of timeless triumph."
        ),
        ZodiacSignInfo(
            sign = "Aquarius",
            symbol = "♒",
            dates = "Jan 20 – Feb 18",
            element = "Air",
            dailyHoroscope = "Cosmic lightning electrifies your unconventional style. Break every mundane rule; shock the hall into awe.",
            luckyColor = "#90CAF9",
            luckyNumber = 11,
            darkPersonality = "The Chaos Oracle: Unpredictable genius born between star-bursts."
        ),
        ZodiacSignInfo(
            sign = "Pisces",
            symbol = "♓",
            dates = "Feb 19 – Mar 20",
            element = "Water",
            dailyHoroscope = "Dive into the haunted oceanic realms of imagination. Your creative soul will cast an unforgettable spell tonight.",
            luckyColor = "#2B1238",
            luckyNumber = 12,
            darkPersonality = "The Siren: Lures mortals into haunting, unforgettable dreams."
        )
    )

    fun getForSign(name: String): ZodiacSignInfo {
        return signs.firstOrNull { it.sign.equals(name, ignoreCase = true) } ?: signs[7] // default Scorpio
    }
}
