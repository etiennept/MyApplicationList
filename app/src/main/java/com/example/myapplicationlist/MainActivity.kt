package com.example.myapplicationlist

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.ApplicationInfo

import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.example.myapplicationlist.ui.theme.MyApplicationListTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp

import androidx.core.graphics.drawable.toBitmap




data class AppInstalledInfo(
    val label: String, val icon: Drawable,
    val packageName: String,
    val startIntent: Intent,
) {
    constructor (app: ApplicationInfo, packageManager: PackageManager, intent: Intent) : this(
        app.loadLabel(packageManager).toString(),
        app.loadIcon(packageManager),
        app.packageName, intent,
    )

}

fun AppInstalledInfo.googlePlay() =
    Intent(
        Intent.ACTION_VIEW,
        Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
    )

fun List<AppInstalledInfo>.verif(text: String) = filter {
    Regex(text.toLowerCase()).containsMatchIn(it.label.toLowerCase())
}
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationListTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    View()
                }
            }
        }
    }
}

@Composable
fun View() {
    val activity = LocalContext.current


    val appsInstalled =
        activity.packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            .map { appInfo ->
                return@map activity.packageManager.getLaunchIntentForPackage(appInfo.packageName)
                    ?.let {

                        return@let AppInstalledInfo(appInfo, activity.packageManager, it)
                    }
            }.filterNotNull()


    var app by rememberSaveable { mutableStateOf("") }
    var text by rememberSaveable { mutableStateOf("") }
    var appsInstalledVisible by remember { mutableStateOf(appsInstalled.verif(text)) }

    Column {
        TextField(
            value = text,
            onValueChange = {
                text = it
                appsInstalledVisible = appsInstalled.verif(text)
            },
            Modifier.fillMaxWidth(),
            label = {
                Text(text = stringResource(id = R.string.search_app))
            })

        LazyColumn {
            items(appsInstalledVisible) {
                Column {
                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            app = it.packageName
                        }) {
                        Image(
                            bitmap = it.icon.toBitmap().asImageBitmap(),
                            contentDescription = "App icon",
                            modifier = Modifier.align(Alignment.CenterStart)
                        )
                        Text(
                            text = it.label,
                            modifier = Modifier.align(
                                Alignment.Center
                            ) , fontSize = 20.sp
                        )
                    }
                    DropdownMenu(
                        expanded = app == it.packageName,
                        onDismissRequest = { app = "" },
                        modifier = Modifier
                    )
                    {
                        DropdownMenuItem(onClick = {
                            app = ""
                            activity.startActivity(it.startIntent)

                        }) {
                            Text(stringResource(id = R.string.open))
                        }
                        DropdownMenuItem(onClick = {
                            activity.startActivity(it.googlePlay( ))
                        }) {

                            Text(stringResource(id = R.string.open))
                        }
                    }
                }
            }
        }
    }

}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MyApplicationListTheme {
        View()
    }
}