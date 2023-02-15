package ru.gosuslugi.epgu.sql.epgusqlintellijplugin

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.LocalQuickFixProvider
import com.intellij.lang.properties.references.PropertiesPsiCompletionUtil
import com.intellij.lang.properties.references.PropertyReferenceBase
import com.intellij.lang.properties.xml.XmlPropertiesFile
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.roots.SourceFolder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.xml.XmlFile
import kotlin.collections.HashSet

class PlaceholderPropertyReference private constructor(
    key: String, element: PsiElement, textRange: TextRange
) : PropertyReferenceBase(key, true, element, textRange) {

    override fun getVariants(): Array<Any> {
        val variants = HashSet<Any>()
        val propertiesFiles = propertiesFiles
        propertiesFiles.forEach { PropertiesPsiCompletionUtil.addVariantsFromFile(this, it, variants) }
        return variants.toArray()
    }

    override fun getPropertiesFiles(): List<XmlPropertiesFile> {
        val module = ModuleUtilCore.findModuleForPsiElement(element) ?: return emptyList()
        return getConfigDirectories(module)
            .flatMap { it.files.toList() }
            .filter { it is XmlFile && it.getName().endsWith("xml") }
            .map { XmlPropertiesFileImpl(it as XmlFile) }
            .toList()
    }

    companion object {

        fun create(element: PsiElement, textRange: TextRange, text: String): PlaceholderPropertyReference {
            return PlaceholderPropertyReference(text, element, textRange)
        }

        private fun getConfigDirectories(module: Module): Set<PsiDirectory> {
            val configs = HashSet<PsiDirectory>()
            collectConfigDirectories(module, configs)
            return configs
        }

        private fun collectConfigDirectories(
            module: Module,
            configs: MutableSet<in PsiDirectory>,
            visitedModules: MutableSet<in Module> = HashSet()
        ) {
            if (!visitedModules.add(module)) return

            val moduleRootManager = ModuleRootManager.getInstance(module)
            moduleRootManager.contentEntries
                .flatMap { it.sourceFolders.asSequence() }
                .forEach { processFolder(it, module, configs) }

            for (dependentModule in moduleRootManager.dependencies) {
                collectConfigDirectories(dependentModule, configs, visitedModules)
            }
        }

        private fun processFolder(folder: SourceFolder, module: Module, configs: MutableSet<in PsiDirectory>) {
            if (folder.file?.isDirectory == true) {
                PsiManager.getInstance(module.project).findDirectory(folder.file!!)
                    ?.findSubdirectory("sql")
                    ?.also { configs.add(it) }
            }
        }
    }
}