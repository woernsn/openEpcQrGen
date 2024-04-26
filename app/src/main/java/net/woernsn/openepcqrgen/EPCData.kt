package net.woernsn.openepcqrgen

class EPCData(
    private val serviceTag: ServiceTag = ServiceTag.BCD,
    private val version: Version = Version.VERSION_002,
    private val characterSet: CharacterSet = CharacterSet.UTF_8,
    private val identification: Identification = Identification.SCT,
    private val bic: String = "",
    private val name: String,
    private val iban: String,
    private val currency: String = "EUR",
    private val amount: Double,
    private val reason: String = "CHAR",
    private val refOfInvoice: String? = "",
    private val text: String? ="",
    private val information: String? ="",
) {
    init {
        if (version == Version.VERSION_001 && bic.isEmpty()) {
            throw Exception("In version 001, the BIC is mandatory!")
        }

        if (name.length > 70) {
            throw Exception("Name must be maximum 70 chars!")
        }

        if (iban.replace(" ", "").length < 16) {
            throw Exception("IBAN must be minimum 16 chars!")
        }

        if (!(0.01..999999999.99).contains(amount)) {
            throw Exception("Amount must be between 0.01 and 999999999.99!")
        }

        if ((refOfInvoice.isNullOrEmpty() && text.isNullOrEmpty())
            || (!refOfInvoice.isNullOrEmpty() && !text.isNullOrEmpty())) {
            throw Exception("Either Reference or Text must be given")
        }

        if (!refOfInvoice.isNullOrEmpty() && refOfInvoice.length > 25) {
            throw Exception("Reference must be maximum 70 chars!")
        }

        if (!text.isNullOrEmpty() && text.length > 140) {
            throw Exception("Text must be maximum 140 chars!")
        }

        if (!information.isNullOrEmpty() && information.length > 70) {
            throw Exception("Information must be maximum 70 chars!")
        }
    }

    override fun toString(): String {
        return buildString {
            appendLine(serviceTag)
            appendLine(version.value)
            appendLine(characterSet.value)
            appendLine(identification)
            appendLine(bic)
            appendLine(name)
            appendLine(iban)
            appendLine("$currency${amount.toBigDecimal().setScale(2).toDouble()}")
            appendLine(reason)
            appendLine(refOfInvoice)
            appendLine(text)
            append(information)
        }.toString()
    }
}

enum class ServiceTag {
    BCD,
}

enum class Version(val value: String) {
    VERSION_001("001"),
    VERSION_002("002"),
}

enum class CharacterSet(val value: Int) {
    UTF_8(1),
    ISO_8859_1(2),
    ISO_8859_2(3),
    ISO_8859_4(4),
    ISO_8859_5(5),
    ISO_8859_7(6),
    ISO_8859_10(7),
    ISO_8859_15(8),
}

enum class Identification {
    SCT, // SEPA Credit Transfer
}