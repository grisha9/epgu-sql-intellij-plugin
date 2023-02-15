package ru.gosuslugi.epgu.sql.epgusqlintellijplugin

import com.intellij.openapi.util.TextRange
import com.intellij.openapi.util.registry.Registry
import com.intellij.patterns.PsiJavaPatterns
import com.intellij.patterns.PsiMethodPattern
import com.intellij.patterns.uast.injectionHostUExpression
import com.intellij.psi.*
import com.intellij.util.SmartList
import ru.gosuslugi.epgu.sql.epgusqlintellijplugin.PlaceholderPropertyReference.Companion.create

class EpguSqlPropertyReferenceContributor : PsiReferenceContributor() {

    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        val injection = injectionHostUExpression()

        for (psiMethodPattern in getMethodPatterns()) {
            registrar.registerUastReferenceProvider(
                injection.methodCallParameter(0, psiMethodPattern, true),
                uastInjectionHostReferenceProvider { _, host -> createPlaceholderPropertiesReferences(host) },
                PsiReferenceRegistrar.LOWER_PRIORITY
            )
        }
    }

    private fun getMethodPatterns(): List<PsiMethodPattern> {
        val getSqlMethodPattern = PsiJavaPatterns.psiMethod().withName("getSql")
            .definedInClass("ru.atc.carcass.common.sql.SqlResolver")
        try {
            return Registry.stringValue("sql.support.classes")
                .split(";")
                .filter { it.isNotEmpty() }
                .map { PsiJavaPatterns.psiMethod().withName("getSql").definedInClass(it) }
                .toList() + getSqlMethodPattern
        } catch (e: Exception) {
            return listOf(getSqlMethodPattern)
        }
    }

    private fun createPlaceholderPropertiesReferences(host: PsiLanguageInjectionHost): Array<PsiReference> {
        return createPlaceholderPropertiesReferences(getFullTextRange(host), host)
    }

    private fun createPlaceholderPropertiesReferences(
        textRanges: Pair<TextRange, String>, valueElement: PsiElement
    ): Array<PsiReference> {
        if (textRanges.second.isEmpty()) return PsiReference.EMPTY_ARRAY
        return SmartList<PsiReference>(create(valueElement, textRanges.first, textRanges.second)).toTypedArray()
    }

    private fun getFullTextRange(element: PsiElement): Pair<TextRange, String> {
        val text = ElementManipulators.getValueText(element)
        val textRange = ElementManipulators.getValueTextRange(element)
        return Pair(textRange, text)
    }
}