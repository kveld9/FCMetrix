package com.kveld9.fcmetrix.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kveld9.fcmetrix.R

@Composable
fun Footer() {
    val uriHandler = LocalUriHandler.current
    val fullText = stringResource(R.string.made_by)
    val handle = "@KVELD9"
    val startIndex = fullText.indexOf(handle)

    val annotatedString = buildAnnotatedString {
        if (startIndex != -1) {
            withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))) {
                append(fullText.substring(0, startIndex))
            }
            withStyle(
                style = SpanStyle(
                    textDecoration = TextDecoration.Underline,
                    fontWeight = FontWeight.Black
                )
            ) {
                append(handle)
            }
        } else {
            append(fullText)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp, bottom = 64.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { uriHandler.openUri("https://github.com/kveld9") },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_github),
                contentDescription = "GitHub",
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = annotatedString,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 2.sp,
                style = TextStyle(
                    shadow = Shadow(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                        blurRadius = 8f
                    )
                )
            )
        }
        Text(
            text = stringResource(R.string.open_source_label),
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
            letterSpacing = 1.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
