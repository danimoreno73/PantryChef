package com.pantrychef.front.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pantrychef.front.theme.PantryChefTheme
import com.pantrychef.front.theme.TextSecondary

@Composable
fun InputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    isError: Boolean = false,
    errorMessage: String? = null,
    placeholder: String? = null,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = { newValue ->
                // Filtrar input según el tipo de teclado
                val filtered = when (keyboardType) {
                    KeyboardType.Decimal -> {
                        // Solo números y un punto decimal
                        if (newValue.isEmpty()) {
                            newValue
                        } else {
                            // Eliminar cualquier carácter que no sea dígito o punto
                            val cleaned = newValue.filter { it.isDigit() || it == '.' }

                            // Asegurar que solo haya un punto decimal
                            val dotCount = cleaned.count { it == '.' }
                            if (dotCount <= 1) {
                                cleaned
                            } else {
                                // Si hay más de un punto, mantener el valor anterior
                                value
                            }
                        }
                    }
                    KeyboardType.Number -> {
                        // Solo números enteros (sin punto decimal)
                        newValue.filter { it.isDigit() }
                    }
                    else -> newValue // Sin filtro para otros tipos
                }

                onValueChange(filtered)
            },
            label = { Text(label) },
            placeholder = placeholder?.let { { Text(it) } },
            leadingIcon = leadingIcon?.let {
                { Icon(imageVector = it, contentDescription = null) }
            },
            trailingIcon = trailingIcon,
            modifier = Modifier.fillMaxWidth(),
            singleLine = singleLine,
            minLines = minLines,
            maxLines = maxLines,
            isError = isError,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            visualTransformation = visualTransformation,
            shape = MaterialTheme.shapes.medium,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = TextSecondary.copy(alpha = 0.3f)
            )
        )

        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

@Preview
@Composable
private fun InputFieldPreview() {
    PantryChefTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            InputField(
                value = "",
                onValueChange = {},
                label = "Correo electrónico",
                placeholder = "tu@correo.com"
            )

            InputField(
                value = "",
                onValueChange = {},
                label = "Cantidad",
                placeholder = "2.5",
                keyboardType = KeyboardType.Decimal,
                modifier = Modifier.padding(top = 16.dp)
            )

            InputField(
                value = "",
                onValueChange = {},
                label = "Descripción",
                placeholder = "Escribe aquí...",
                singleLine = false,
                minLines = 3,
                maxLines = 6,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}