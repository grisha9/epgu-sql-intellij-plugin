package ru.gosuslugi.epgu.sql.epgusqlintellijplugin


import com.intellij.lang.properties.IProperty
import com.intellij.lang.properties.psi.PropertiesFile
import com.intellij.lang.properties.xml.XmlPropertiesFile
import com.intellij.pom.references.PomService
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiInvalidElementAccessException
import com.intellij.psi.PsiTarget
import com.intellij.psi.meta.PsiMetaData
import com.intellij.psi.xml.XmlTag
import com.intellij.util.IncorrectOperationException
import com.intellij.util.PlatformIcons
import org.jetbrains.annotations.NonNls
import javax.swing.Icon

class XmlSqlProperty(private val sqlTag: XmlTag, private val propertiesFile: XmlPropertiesFile?) : IProperty,
    PsiTarget, PsiMetaData {
    override fun getName(): String? {
        return sqlTag.getAttributeValue("name")
    }

    override fun init(element: PsiElement?) {
    }

    override fun setName(name: String): PsiElement? {
        return sqlTag.setAttribute("name", name)
    }

    override fun getKey(): String? {
        return name
    }

    override fun getValue(): String {
        return sqlTag.value.text
    }

    override fun getUnescapedValue(): String? {
        return value
    }

    override fun getUnescapedKey(): String? {
        return key
    }

    @Throws(IncorrectOperationException::class)
    override fun setValue(@NonNls value: String) {
        sqlTag.value.text = value
    }

    @Throws(PsiInvalidElementAccessException::class)
    override fun getPropertiesFile(): PropertiesFile? {
        return propertiesFile
    }

    override fun getDocCommentText(): String? {
        return null
    }

    override fun getPsiElement(): PsiElement {
        return PomService.convertToPsi(this)
    }

    override fun navigate(requestFocus: Boolean) {}

    override fun canNavigate(): Boolean {
        return true
    }

    override fun canNavigateToSource(): Boolean {
        return true
    }

    override fun getIcon(flags: Int): Icon? {
        return PlatformIcons.PROPERTY_ICON
    }

    override fun isValid(): Boolean {
        return sqlTag.isValid
    }

    override fun getNavigationElement(): PsiElement {
        return sqlTag
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val property = o as XmlSqlProperty
        return if (sqlTag != property.sqlTag) false else true
    }

    override fun hashCode(): Int {
        return sqlTag.hashCode()
    }

    override fun toString(): String {
        return "XmlProperty: key = '$key', value = '$value'"
    }

    override fun getDeclaration(): PsiElement {
        return sqlTag
    }

    override fun getName(context: PsiElement?): String {
        return key ?: ""
    }
}