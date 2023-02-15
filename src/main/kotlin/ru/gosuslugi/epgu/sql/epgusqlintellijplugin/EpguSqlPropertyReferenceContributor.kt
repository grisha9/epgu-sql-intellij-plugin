package ru.gosuslugi.epgu.sql.epgusqlintellijplugin

import com.intellij.openapi.util.TextRange
import com.intellij.patterns.PsiJavaPatterns
import com.intellij.patterns.uast.injectionHostUExpression
import com.intellij.psi.*
import com.intellij.util.SmartList
import java.util.Collections.singletonMap

class EpguSqlPropertyReferenceContributor : PsiReferenceContributor() {

    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        val injection = injectionHostUExpression()
        val getSqlMethodPattern = PsiJavaPatterns.psiMethod().withName("getSql")
            .withParameterCount(1)
            .withParameters(CommonClassNames.JAVA_LANG_STRING)
            .definedInClass("ru.atc.carcass.common.sql.SqlResolver")

        registrar.registerUastReferenceProvider(
            injection.methodCallParameter(0, getSqlMethodPattern, false),
            uastInjectionHostReferenceProvider { _, host -> createPlaceholderPropertiesReferences(host) },
            PsiReferenceRegistrar.LOWER_PRIORITY
        )
    }

    private fun createPlaceholderPropertiesReferences(host: PsiLanguageInjectionHost): Array<PsiReference> {
        return createPlaceholderPropertiesReferences(getFullTextRange(host), host)
    }

    private fun createPlaceholderPropertiesReferences(
        textRanges: Map<TextRange, String>,
        valueElement: PsiElement?
    ): Array<PsiReference> {
        if (valueElement == null || textRanges.isEmpty()) return PsiReference.EMPTY_ARRAY

        val references = SmartList<PsiReference>()

        for ((textRange, info) in textRanges) {
            references.add(PlaceholderPropertyReference.create(valueElement, textRange, info))
        }
        return references.toTypedArray()
    }

    private fun getFullTextRange(element: PsiElement): Map<TextRange, String> {
        val text = ElementManipulators.getValueText(element)
        val textRange = ElementManipulators.getValueTextRange(element)
        return singletonMap(textRange, text)
    }
}