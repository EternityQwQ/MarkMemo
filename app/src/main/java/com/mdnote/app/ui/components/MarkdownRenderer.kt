package com.mdnote.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 简易的 Markdown 渲染器
 * 支持：标题、粗体、斜体、删除线、代码块、引用、列表、链接、分割线
 */
@Composable
fun MarkdownRenderer(
    markdown: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        val lines = markdown.split("\n")
        var inCodeBlock = false
        val codeBlockContent = StringBuilder()
        var inBlockquote = false
        val blockquoteContent = StringBuilder()

        for (line in lines) {
            // Code block
            if (line.trimStart().startsWith("```")) {
                if (inCodeBlock) {
                    // End code block
                    renderCodeBlock(codeBlockContent.toString())
                    codeBlockContent.clear()
                    inCodeBlock = false
                } else {
                    // Start code block
                    inCodeBlock = true
                }
                continue
            }

            if (inCodeBlock) {
                codeBlockContent.appendLine(line)
                continue
            }

            // Blockquote
            if (line.trimStart().startsWith("> ")) {
                if (!inBlockquote) {
                    inBlockquote = true
                }
                blockquoteContent.appendLine(line.trimStart().removePrefix("> "))
                continue
            } else if (inBlockquote) {
                renderBlockquote(blockquoteContent.toString())
                blockquoteContent.clear()
                inBlockquote = false
            }

            // Horizontal rule
            if (line.trim() == "---" || line.trim() == "***" || line.trim() == "___") {
                renderHorizontalRule()
                continue
            }

            // Heading
            when {
                line.startsWith("###### ") -> renderHeading(line.removePrefix("###### "), 6)
                line.startsWith("##### ") -> renderHeading(line.removePrefix("##### "), 5)
                line.startsWith("#### ") -> renderHeading(line.removePrefix("#### "), 4)
                line.startsWith("### ") -> renderHeading(line.removePrefix("### "), 3)
                line.startsWith("## ") -> renderHeading(line.removePrefix("## "), 2)
                line.startsWith("# ") -> renderHeading(line.removePrefix("# "), 1)
                line.startsWith("- ") || line.startsWith("* ") -> renderListItem(line.removePrefix("- ").removePrefix("* "))
                line.startsWith("1. ") || line.matches(Regex("^\\d+\\.\\s.*")) -> {
                    val content = line.replaceFirst(Regex("^\\d+\\.\\s"), "")
                    renderListItem(content)
                }
                line.isBlank() -> Spacer(modifier = Modifier.height(8.dp))
                else -> renderParagraph(line)
            }
        }

        // Handle remaining blockquote
        if (inBlockquote && blockquoteContent.isNotEmpty()) {
            renderBlockquote(blockquoteContent.toString())
        }
    }
}

@Composable
private fun renderHeading(text: String, level: Int) {
    val fontSize = when (level) {
        1 -> 28.sp
        2 -> 24.sp
        3 -> 20.sp
        4 -> 18.sp
        5 -> 16.sp
        6 -> 14.sp
        else -> 20.sp
    }

    Spacer(modifier = Modifier.height(if (level == 1) 16.dp else 8.dp))
    Text(
        text = buildAnnotatedString {
            renderInlineMarkdown(text)
        },
        fontSize = fontSize,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
    )
    Spacer(modifier = Modifier.height(4.dp))
}

@Composable
private fun renderParagraph(text: String) {
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        text = buildAnnotatedString {
            renderInlineMarkdown(text)
        },
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
private fun renderListItem(text: String) {
    Spacer(modifier = Modifier.height(2.dp))
    Text(
        text = buildAnnotatedString {
            append("  \u2022  ")
            renderInlineMarkdown(text)
        },
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
private fun renderCodeBlock(code: String) {
    Spacer(modifier = Modifier.height(8.dp))
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = code.trimEnd(),
            modifier = Modifier.padding(12.dp),
            fontFamily = FontFamily.Monospace,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
private fun renderBlockquote(content: String) {
    Spacer(modifier = Modifier.height(8.dp))
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            Surface(
                modifier = Modifier
                    .width(3.dp)
                    .height(40.dp),
                color = MaterialTheme.colorScheme.primary
            ) {}
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = content.trimEnd(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
private fun renderHorizontalRule() {
    Spacer(modifier = Modifier.height(8.dp))
    HorizontalDivider(
        color = MaterialTheme.colorScheme.outlineVariant,
        modifier = Modifier.padding(vertical = 4.dp)
    )
    Spacer(modifier = Modifier.height(8.dp))
}

/**
 * 解析内联 Markdown 样式：粗体、斜体、删除线、行内代码、链接
 */
private fun androidx.compose.ui.text.AnnotatedString.Builder.renderInlineMarkdown(text: String) {
    var remaining = text
    while (remaining.isNotEmpty()) {
        when {
            remaining.startsWith("**") -> {
                val end = remaining.indexOf("**", 2)
                if (end != -1) {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(remaining.substring(2, end))
                    }
                    remaining = remaining.substring(end + 2)
                } else {
                    append(remaining)
                    return
                }
            }
            remaining.startsWith("__") -> {
                val end = remaining.indexOf("__", 2)
                if (end != -1) {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(remaining.substring(2, end))
                    }
                    remaining = remaining.substring(end + 2)
                } else {
                    append(remaining)
                    return
                }
            }
            remaining.startsWith("*") && remaining.length > 1 && remaining[1] != ' ' -> {
                val end = remaining.indexOf("*", 1)
                if (end != -1) {
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                        append(remaining.substring(1, end))
                    }
                    remaining = remaining.substring(end + 1)
                } else {
                    append(remaining.first())
                    remaining = remaining.substring(1)
                }
            }
            remaining.startsWith("_") && remaining.length > 1 && remaining[1] != ' ' -> {
                val end = remaining.indexOf("_", 1)
                if (end != -1) {
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                        append(remaining.substring(1, end))
                    }
                    remaining = remaining.substring(end + 1)
                } else {
                    append(remaining.first())
                    remaining = remaining.substring(1)
                }
            }
            remaining.startsWith("~~") -> {
                val end = remaining.indexOf("~~", 2)
                if (end != -1) {
                    withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) {
                        append(remaining.substring(2, end))
                    }
                    remaining = remaining.substring(end + 2)
                } else {
                    append(remaining)
                    return
                }
            }
            remaining.startsWith("`") -> {
                val end = remaining.indexOf("`", 1)
                if (end != -1) {
                    withStyle(
                        SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            background = androidx.compose.ui.graphics.Color(0x20000000)
                        )
                    ) {
                        append(remaining.substring(1, end))
                    }
                    remaining = remaining.substring(end + 1)
                } else {
                    append(remaining)
                    return
                }
            }
            remaining.startsWith("[") -> {
                val bracketEnd = remaining.indexOf("](")
                val parenEnd = if (bracketEnd != -1) remaining.indexOf(")", bracketEnd) else -1
                if (bracketEnd != -1 && parenEnd != -1) {
                    withStyle(
                        SpanStyle(
                            color = MaterialTheme.colorScheme.primary,
                            textDecoration = TextDecoration.Underline
                        )
                    ) {
                        append(remaining.substring(1, bracketEnd))
                    }
                    remaining = remaining.substring(parenEnd + 1)
                } else {
                    append(remaining.first())
                    remaining = remaining.substring(1)
                }
            }
            else -> {
                append(remaining.first())
                remaining = remaining.substring(1)
            }
        }
    }
}