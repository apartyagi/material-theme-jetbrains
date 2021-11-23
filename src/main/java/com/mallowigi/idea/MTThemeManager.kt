/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015-2021 Elior "Mallowigi" Boukhobza
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 *
 */
package com.mallowigi.idea

import com.intellij.ide.ui.LafManager
import com.intellij.ide.ui.UISettings
import com.intellij.ide.ui.UITheme
import com.intellij.ide.ui.laf.LafManagerImpl
import com.intellij.ide.ui.laf.UIThemeBasedLookAndFeelInfo
import com.intellij.ide.ui.laf.darcula.DarculaInstaller
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.colors.FontPreferences
import com.intellij.openapi.editor.colors.impl.AppEditorFontOptions
import com.intellij.openapi.editor.colors.impl.EditorColorsManagerImpl
import com.intellij.openapi.fileTypes.ex.FileTypeManagerEx
import com.intellij.openapi.util.Couple
import com.intellij.openapi.util.IconLoader
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.util.registry.Registry
import com.intellij.ui.ColorUtil
import com.intellij.util.ModalityUiUtil
import com.intellij.util.ObjectUtils
import com.intellij.util.SVGLoader
import com.intellij.util.containers.ContainerUtil
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import com.mallowigi.idea.config.application.MTConfig
import com.mallowigi.idea.listeners.MTTopics
import com.mallowigi.idea.themes.MTThemeFacade
import com.mallowigi.idea.themes.MTThemes
import com.mallowigi.idea.themes.lists.AccentResources
import com.mallowigi.idea.themes.lists.FontResources
import com.mallowigi.idea.utils.MTStyledKitPatcher
import com.mallowigi.idea.utils.MTUI
import com.mallowigi.idea.utils.MTUiUtils
import com.mallowigi.idea.utils.animator.MTChangeLafService.hideSnapshotWithAnimation
import com.mallowigi.idea.utils.animator.MTChangeLafService.showSnapshot
import org.jetbrains.annotations.NonNls
import java.awt.Color
import java.awt.Font
import java.util.Locale
import javax.swing.SwingUtilities
import javax.swing.UIDefaults
import javax.swing.UIManager
import javax.swing.plaf.FontUIResource

class MTThemeManager private constructor() : Disposable {
  override fun dispose(): Unit = Unit

  //region Action Toggles

  /**
   * Toggle colored dirs
   *
   */
  fun toggleColoredDirs() {
    CONFIG.isUseColoredDirectories = !CONFIG.isUseColoredDirectories
    updateFileIcons()
  }

  /**
   * Toggle code additions
   *
   */
  fun toggleCodeAdditions() {
    CONFIG.isCodeAdditionsEnabled = !CONFIG.isCodeAdditionsEnabled
    refreshColorScheme()
  }

  /**
   * Set contrast and reactivate theme
   */
  fun toggleContrast() {
    CONFIG.isContrastMode = !CONFIG.isContrastMode
    applyContrast(true)
  }

  /**
   * Toggle high contrast.
   */
  fun toggleHighContrast() {
    CONFIG.isHighContrast = !CONFIG.isHighContrast
    activate()
  }

  /**
   * Toggle overlays
   *
   */
  fun toggleOverlays() {
    CONFIG.isShowOverlays = !CONFIG.isShowOverlays
  }

  /**
   * Toggle compact status bar.
   */
  fun toggleCompactStatusBar() {
    val compactStatusBar = CONFIG.isCompactStatusBar
    CONFIG.isCompactStatusBar = !compactStatusBar
    applyCompactToolWindowHeaders()
  }

  /**
   * Toggle compact sidebar.
   */
  fun toggleCompactSidebar() {
    val isCompactSidebar = CONFIG.isCompactSidebar
    CONFIG.isCompactSidebar = !isCompactSidebar
    applyCompactSidebar(true)
  }

  /**
   * Toggle compact dropdowns.
   */
  fun toggleCompactDropdowns() {
    val isCompactDropdowns = CONFIG.isCompactDropdowns
    CONFIG.isCompactDropdowns = !isCompactDropdowns
    applyDropdownLists()
    UIReplacer.patchUI()
  }

  /**
   * Toggle compact menus.
   */
  fun toggleCompactMenus() {
    val isCompact = CONFIG.isCompactMenus
    CONFIG.isCompactMenus = !isCompact
    applyMenusHeight()
    UIReplacer.patchUI()
  }

  /**
   * Toggle Compact table cells
   */
  fun toggleCompactTableCells() {
    val isCompact = CONFIG.isCompactTables
    CONFIG.isCompactTables = !isCompact
    reloadUI()
  }

  /**
   * Toggle custom tab font
   *
   */
  fun toggleCustomTabFont() {
    val tabFontSizeEnabled = CONFIG.isTabFontSizeEnabled
    CONFIG.isTabFontSizeEnabled = !tabFontSizeEnabled
    reloadUI()
  }

  /**
   * Toggle custom tree font
   *
   */
  fun toggleCustomTreeFont() {
    val treeFontSizeEnabled = CONFIG.isTreeFontSizeEnabled
    CONFIG.isTreeFontSizeEnabled = !treeFontSizeEnabled
    reloadUI()
  }

  /**
   * Toggle material fonts (Roboto).
   */
  fun toggleMaterialFonts() {
    val useMaterialFonts = CONFIG.isUseMaterialFont
    CONFIG.isUseMaterialFont = !useMaterialFonts
    applyFonts()
  }

  /**
   * Toggle material wallpapers.
   */
  fun toggleMaterialWallpapers() {
    CONFIG.isUseMaterialWallpapers = !CONFIG.isUseMaterialWallpapers
    CONFIG.fireChanged()
  }

  /**
   * Toggle upper case tabs.
   */
  fun toggleUpperCaseTabs() {
    CONFIG.isUpperCaseTabs = !CONFIG.isUpperCaseTabs
    CONFIG.fireChanged()
  }

  /**
   * Toggle override accent color
   */
  fun toggleOverrideAccent() {
    CONFIG.isOverrideAccentColor = !CONFIG.isOverrideAccentColor
    CONFIG.fireChanged()
  }

  /**
   * Toggle project frame
   */
  fun toggleProjectFrame() {
    CONFIG.isUseProjectFrame = !CONFIG.isUseProjectFrame
    CONFIG.fireChanged()
  }

  /**
   * Toggle outlined buttons
   *
   */
  fun toggleOutlinedButtons() {
    CONFIG.isBorderedButtons = !CONFIG.isBorderedButtons
    CONFIG.fireChanged()
  }

  /**
   * Toggle striped tool windows
   */
  fun toggleStripedToolWindows() {
    CONFIG.isStripedToolWindowsEnabled = !CONFIG.isStripedToolWindowsEnabled
    applyStripedToolWindows()
  }

  /**
   * Apply compact tool window headers
   *
   */
  private fun applyCompactToolWindowHeaders() {
    val vPad = if (CONFIG.isCompactStatusBar) JBUI.scale(0) else JBUI.scale(5)
    UIManager.put(MTUI.Panel.TOOL_WINDOW_TAB_VERTICAL_PADDING, vPad)
  }

  /**
   * Apply striped tool windows in the Registry
   *
   */
  private fun applyStripedToolWindows() = Registry.get(NEW_STRIPES_UI).setValue(CONFIG.isStripedToolWindowsEnabled)

  /**
   * Refresh trees
   */
  private fun updateFileIcons() {
    ModalityUiUtil.invokeLaterIfNeeded(ModalityState.NON_MODAL) {
      ApplicationManager.getApplication().runWriteAction { FileTypeManagerEx.getInstanceEx().fireFileTypesChanged() }
    }
  }
  //endregion

  //region Theme activation and deactivation

  /**
   * Activate selected theme or deactivate current
   */
  fun activate() {
    val mtTheme = CONFIG.selectedTheme
    activate(mtTheme)
  }

  /**
   * Activate a theme
   *
   * @param themeId theme id
   * @param isDark  dark
   * @param name    name
   */
  fun activateLAF(themeId: String, isDark: Boolean, name: String) {
    val themeFor = MTThemes.getThemeFor(themeId)
    if (themeFor != null) {
      activate(themeFor)
    } else {
      val mtTheme: MTThemeFacade = MTThemes.NATIVE
      mtTheme.setIsDark(isDark)
      mtTheme.themeName = name
      activate(mtTheme)
    }
  }

  /**
   * Checks whether Look and feel is material theme
   *
   * @param theme
   * @return
   */
  fun isMaterialTheme(theme: @NonNls UIManager.LookAndFeelInfo?): Boolean =
    theme is UIThemeBasedLookAndFeelInfo && MTThemes.getThemeFor(theme.theme.id) != null

  /**
   * Activate a Look and Feel
   *
   * @param theme UITheme
   */
  fun activateLAF(theme: UITheme): Unit = activateLAF(theme.id, theme.isDark, theme.name)

  /**
   * Activate theme and switch color scheme
   *
   * @param mtTheme the mt theme
   */
  fun activate(mtTheme: MTThemeFacade?) {
    var newTheme = mtTheme
    if (newTheme == null) {
      newTheme = MTThemes.OCEANIC
    }
    CONFIG.selectedTheme = newTheme
    newTheme.theme.activate()

    // Save a reference to the theme
    IconLoader.clearCache()

    // apply different settings
    applyContrast(false)
    applyCompactSidebar(false)
    applyCustomTreeIndent()
    applyMenusHeight()
    applyDropdownLists()
    applyAccents(false)
    applyFonts()
    applyCompactToolWindowHeaders()
    applyStripedToolWindows()

    // Documentation styles
    patchStyledEditorKit()

    // Monochrome filter and co
    LafManager.getInstance().updateUI()
    // Custom UI Patches
    UIReplacer.patchUI()
    fireThemeChanged(newTheme)
  }

  /**
   * Refresh color scheme
   *
   */
  private fun refreshColorScheme() {
    ApplicationManager.getApplication()
      .invokeLater({ (EditorColorsManager.getInstance() as EditorColorsManagerImpl).schemeChangedOrSwitched(null) },
                   ModalityState.NON_MODAL)
  }

  /**
   * New way of switching themes
   */
  fun setLookAndFeel(selectedTheme: MTThemeFacade) {
    // Find LAF theme and trigger a theme change
    val lafManager = LafManager.getInstance()
    val lafInfo = ContainerUtil.find(lafManager.installedLookAndFeels) {
      it.name == selectedTheme.themeName
    }

    showSnapshot()

    if (lafInfo != null) {
      lafManager.currentLookAndFeel = lafInfo
    } else {
      activate(selectedTheme)
    }

    SwingUtilities.invokeLater { hideSnapshotWithAnimation() }
  }

  /**
   * Apply accents.
   */
  fun applyAccents(fireEvent: Boolean) {
    val accentColor = ColorUtil.fromHex(CONFIG.accentColor)
    val transparentAccentColor = ColorUtil.toAlpha(accentColor, 70)

    AccentResources.ACCENT_RESOURCES.forEach { UIManager.put(it, accentColor) }

    AccentResources.ACCENT_TRANSPARENT_RESOURCES.forEach { UIManager.put(it, transparentAccentColor) }

    // Accent mode
    CONFIG.selectedTheme.applyAccentMode()
    // Scrollbars management
    applyScrollbars(accentColor)
    // Documentation
    patchStyledEditorKit()
    // Icons
    addAccentColorTint()

    if (fireEvent) {
      fireAccentChanged(accentColor)
    }
  }

  /**
   * Apply scrollbars accent color
   *
   * @param accentColor
   */
  private fun applyScrollbars(accentColor: Color) {
    val scrollbarColors = getScrollbarColors(accentColor)
    val scrollbarColor = scrollbarColors.getFirst()
    val scrollbarHoverColor = scrollbarColors.getSecond()

    AccentResources.SCROLLBAR_RESOURCES.forEach { UIManager.put(it, scrollbarColor) }

    AccentResources.SCROLLBAR_HOVER_RESOURCES.forEach { UIManager.put(it, scrollbarHoverColor) }

    reloadUI()
  }

  /**
   * Get scrollbar colors
   *
   * @param accentColor
   * @return
   */
  private fun getScrollbarColors(accentColor: Color): Couple<Color> {
    val transAccentColor = ColorUtil.toAlpha(accentColor, 50)
    val hoverAccentColor = ColorUtil.toAlpha(accentColor, 75)
    val themedColor = MTUI.Label.getLabelForeground()
    val transThemedColor = ColorUtil.toAlpha(themedColor, 50)
    val hoverThemedColor = ColorUtil.toAlpha(themedColor, 75)

    return when {
      CONFIG.isAccentScrollbars -> when {
        CONFIG.isThemedScrollbars -> Couple(transAccentColor, hoverAccentColor)
        else                      -> Couple(hoverAccentColor, accentColor)
      }
      else                      -> when {
        CONFIG.isThemedScrollbars -> Couple(transThemedColor, hoverThemedColor)
        else                      -> Couple(hoverThemedColor, themedColor)
      }
    }
  }

  /**
   * Fire theme changed
   *
   * @param newTheme
   */
  private fun fireThemeChanged(newTheme: MTThemeFacade) {
    ApplicationManager.getApplication().messageBus
      .syncPublisher(MTTopics.THEMES)
      .themeChanged(newTheme)
  }

  /**
   * Fire accent changed
   *
   * @param accentColorColor
   */
  private fun fireAccentChanged(accentColorColor: Color) {
    ApplicationManager.getApplication().messageBus
      .syncPublisher(MTTopics.ACCENTS)
      .accentChanged(accentColorColor)
  }

  //endregion

  // region Fonts
  /**
   * Apply custom fonts
   *
   * @param uiDefaults the defaults to override
   * @param fontFace   the font face
   * @param fontSize   the font size
   */
  private fun applySettingsFont(uiDefaults: @NonNls UIDefaults?, fontFace: String?, fontSize: Int) {
    uiDefaults!!["Tree.ancestorInputMap"] = null

    val font = UIUtil.getFontWithFallback(fontFace, Font.PLAIN, fontSize)
    val editorFontName = AppEditorFontOptions.getInstance().fontPreferences.fontFamily
    val monospaceFont = ObjectUtils.notNull(editorFontName, MTConfig.DEFAULT_MONO_FONT)
    val monoFont = FontUIResource(monospaceFont, Font.PLAIN, fontSize)

    // Keep old style and size
    FontResources.FONT_RESOURCES.forEach {
      val curFont = ObjectUtils.notNull(uiDefaults.getFont(it), font)
      uiDefaults[it] = font.deriveFont(curFont.style, curFont.size.toFloat())
    }

    uiDefaults["PasswordField.font"] = monoFont
    uiDefaults["TextArea.font"] = monoFont
    uiDefaults["TextPane.font"] = font
    uiDefaults["EditorPane.font"] = font
  }

  /**
   * Apply material fonts
   *
   * @param uiDefaults
   */
  private fun applyMaterialFonts(uiDefaults: @NonNls UIDefaults?) {
    uiDefaults!!["Tree.ancestorInputMap"] = null

    val language = Locale.getDefault().language
    val cjkLocale =
      Locale.CHINESE.language == language || Locale.JAPANESE.language == language || Locale.KOREAN.language == language
    var font = UIUtil.getFontWithFallback(MTConfig.DEFAULT_FONT, Font.PLAIN, MTConfig.DEFAULT_FONT_SIZE)

    if (cjkLocale) {
      font = UIUtil.getFontWithFallback(MTUiUtils.NOTO_SANS, Font.PLAIN, MTConfig.DEFAULT_FONT_SIZE)
    }

    val uiFont = font
    val textFont = font
    val editorFontName = AppEditorFontOptions.getInstance().fontPreferences.fontFamily
    val monospaceFont = ObjectUtils.notNull(editorFontName, MTConfig.DEFAULT_MONO_FONT)
    val monoFont = FontUIResource(monospaceFont, Font.PLAIN, MTConfig.DEFAULT_FONT_SIZE)

    // Keep old style and size
    FontResources.FONT_RESOURCES.forEach {
      val curFont = ObjectUtils.notNull(uiDefaults.getFont(it), uiFont)
      uiDefaults[it] = uiFont.deriveFont(curFont.style, curFont.size.toFloat())
    }

    uiDefaults["PasswordField.font"] = monoFont
    uiDefaults["TextArea.font"] = monoFont
    uiDefaults["TextPane.font"] = textFont
    uiDefaults["EditorPane.font"] = textFont
  }

  /**
   * Apply fonts according to settings
   */
  private fun applyFonts() {
    val uiSettings: UISettings = UISettings.instance
    val lookAndFeelDefaults = UIManager.getLookAndFeelDefaults()
    val useMaterialFont = CONFIG.isUseMaterialFont

    if (uiSettings.overrideLafFonts) {
      applySettingsFont(lookAndFeelDefaults, uiSettings.fontFace, uiSettings.fontSize)
    } else if (useMaterialFont) {
      applyMaterialFonts(lookAndFeelDefaults)
    } else {
      if (SystemInfo.isMacOSYosemite) {
        LafManagerImpl.installMacOSXFonts(lookAndFeelDefaults)
      }
    }

    applyCustomTreeFont(lookAndFeelDefaults)
    applyGlobalFontSettings()
  }

  /**
   * Apply custom tree font
   *
   * @param lookAndFeelDefaults
   */
  private fun applyCustomTreeFont(lookAndFeelDefaults: @NonNls UIDefaults?) {
    val treeFontSize = JBUI.scale(CONFIG.treeFontSize)
    val treeFont = CONFIG.treeFont

    if (CONFIG.isTreeFontSizeEnabled) {
      val font = lookAndFeelDefaults!!.getFont("Tree.font")
      lookAndFeelDefaults["Tree.font"] = Font(treeFont, font.style, treeFontSize)
      LafManager.getInstance().updateUI()
    }
  }

  /**
   * Apply global font settings
   *
   */
  private fun applyGlobalFontSettings() {
    val currentScheme = MTUiUtils.getCurrentScheme()
    if (CONFIG.isUseGlobalFont) {
      currentScheme.setUseAppFontPreferencesInEditor()
    }
    EditorFactory.getInstance().refreshAllEditors()
  }

  private val fontPreferences: FontPreferences
    get() = MTUiUtils.getCurrentScheme().fontPreferences
  //endregion

  //region Contrast support
  /**
   * Apply contrast
   *
   * @param reloadUI if true, reload the ui
   */
  private fun applyContrast(reloadUI: Boolean) {
    val apply = CONFIG.isContrastMode
    val mtTheme = CONFIG.selectedTheme.theme
    mtTheme.applyContrast(apply)
    if (reloadUI) {
      reloadUI()
    }
  }
  //endregion

  //region Custom tree indents support
  /**
   * Apply custom tree indent
   */
  private fun applyCustomTreeIndent() {
    if (CONFIG.isCustomTreeIndentEnabled) {
      UIManager.put("Tree.leftChildIndent", CONFIG.leftTreeIndent)
      UIManager.put("Tree.rightChildIndent", CONFIG.rightTreeIndent)
    } else {
      UIManager.put("Tree.leftChildIndent", MTConfig.DEFAULT_INDENT / 2 + JBUI.scale(7))
      UIManager.put("Tree.rightChildIndent", MTConfig.DEFAULT_INDENT / 2 + JBUI.scale(4))
    }
  }
  //endregion

  //region Compact Menus support
  /**
   * Apply custom tree indent
   */
  private fun applyMenusHeight() {
    if (CONFIG.isCompactMenus) {
      UIManager.put("PopupMenuSeparator.height", 3)
      UIManager.put("PopupMenuSeparator.stripeIndent", 1)
    } else {
      UIManager.put("PopupMenuSeparator.height", 10)
      UIManager.put("PopupMenuSeparator.stripeIndent", 5)
    }
  }
  //endregion

  //region Compact Dropdowns support
  /**
   * Apply custom tree indent
   */
  private fun applyDropdownLists() {
    if (CONFIG.isCompactDropdowns) {
      UIManager.put("ActionsList.cellBorderInsets", JBUI.insets(1, 10, 1, 15))
    } else {
      UIManager.put("ActionsList.cellBorderInsets", JBUI.insets(5, 10, 5, 15))
    }
  }
  //endregion

  //region Compact Sidebar support
  /**
   * Use compact sidebar option
   */
  private fun applyCompactSidebar(reloadUI: Boolean) {
    val isCustomSidebarHeight = CONFIG.isCompactSidebar
    val customSidebarHeight = CONFIG.customSidebarHeight
    val rowHeight =
      if (isCustomSidebarHeight) JBUI.scale(customSidebarHeight) else JBUI.scale(MTConfig.DEFAULT_SIDEBAR_HEIGHT)

    UIManager.put("Tree.rowHeight", rowHeight)
    if (reloadUI) {
      reloadUI()
    }
  }
  //endregion

  //region Accents supports
  /**
   * Override patch style editor kit for custom accent support
   */
  private fun patchStyledEditorKit() {
    MTStyledKitPatcher.patchStyledEditorKit()
  }

  /**
   * Add accent color tint
   *
   */
  private fun addAccentColorTint() = SVGLoader.setColorPatcherProvider(MTAccentColorPatcher())

  /**
   * Toggle accent mode
   */
  fun toggleAccentMode() {
    CONFIG.isAccentMode = !CONFIG.isAccentMode
    CONFIG.fireChanged()
  }
  //endregion

  //region Tabs Height support
  /**
   * Sets tabs height.
   *
   * @param newTabsHeight the new tabs height
   */
  fun setTabsHeight(newTabsHeight: Int) {
    CONFIG.tabsHeight = newTabsHeight
  }
  //endregion

  /**
   * Trigger a reloadUI event
   */
  private fun reloadUI() {
    applyFonts()
    DarculaInstaller.uninstall()

    if (UIUtil.isUnderDarcula()) {
      DarculaInstaller.install()
    }

    LafManager.getInstance().updateUI()
  }

  /**
   * Clean registry
   *
   */
  fun cleanRegistry() {
    Registry.get(NEW_STRIPES_UI).resetToDefault()
  }

  companion object {
    private val CONFIG = MTConfig.getInstance()
    const val RETINA: String = "@2x.css"
    const val NON_RETINA: String = ".css"
    private const val DARCULA: String = "darcula"

    const val NEW_STRIPES_UI: String = "ide.experimental.ui.toolwindow.stripes"

    val instance: MTThemeManager
      get() = ApplicationManager.getApplication().getService(MTThemeManager::class.java)
  }
}
