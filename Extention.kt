package com.voiceaudiorecorder.recordvoice.accessibility

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.webkit.WebView
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.annotation.*
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.snackbar.Snackbar
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

fun View.makeVisible(){
    View.VISIBLE
}

fun View.makeGone(){
    View.GONE
}

fun Context.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun Any?.printToLog(tag: String = "DEBUG_LOG") {
    Log.d(tag, toString())
}

fun View.hideKeyboard() {
    val inputMethodManager = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
}

fun WebView.loadUrl(url: String?) {
    if (!url.isNullOrEmpty()) {
        loadUrl(url)
    }
}

fun showSnackBar(viewParent: View? , message : String){
    val snack = Snackbar.make(viewParent!!, message, Snackbar.LENGTH_SHORT)
    val view = snack.view
    view.setBackgroundColor(ContextCompat.getColor(viewParent.context , R.color.text))
    val params = view.layoutParams as FrameLayout.LayoutParams
    params.gravity = Gravity.TOP
    view.layoutParams = params
    snack.show()
}

fun Context.isNetworkConnected(): Boolean {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val nw = connectivityManager.activeNetwork ?: return false
        val actNw = connectivityManager.getNetworkCapabilities(nw) ?: return false
        return when {
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            //for other device how are able to connect with Ethernet
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            //for check internet over Bluetooth
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true
            else -> false
        }
    } else {
        val nwInfo = connectivityManager.activeNetworkInfo ?: return false
        return nwInfo.isConnected
    }
}

fun AppCompatEditText.clearText() { setText("") }

fun Context.isPermissionGranted(permission: String): Boolean {
    return ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}

fun Context.drawable(@DrawableRes resId: Int) = ResourcesCompat.getDrawable(resources, resId, null)

fun Context.font(@FontRes resId: Int) = ResourcesCompat.getFont(this, resId)

fun Context.color(@ColorRes resId: Int) = ResourcesCompat.getColor(resources,resId ,null)

fun Context.dimen(@DimenRes resId: Int) = resources.getDimension(resId)

fun Context.anim(@AnimRes resId: Int) = AnimationUtils.loadAnimation(this, resId)

fun getCurrentDateTime(): Date {
    return Calendar.getInstance().time
}//Thu May 04 13:26:08 GMT+05:00 2023
fun Date.getFormattedDate(): String {
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return dateFormat.format(this)
}

fun Drawable.toBitmap(): Bitmap {
    val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    setBounds(0, 0, canvas.width, canvas.height)
    draw(canvas)
    return bitmap
}

fun Context.toDp(px: Int): Float {
    return px / resources.displayMetrics.density
}

fun Context.toPx(dp: Int): Int {
    return (dp * resources.displayMetrics.density).toInt()
}

fun Context.getScreenWidth(): Int {
    val displayMetrics = resources.displayMetrics
    return displayMetrics.widthPixels
}

fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable) {
            afterTextChanged.invoke(s.toString())
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    })
}

fun ViewGroup.inflate(layoutRes: Int): View {
    return LayoutInflater.from(context).inflate(layoutRes, this, false)
}

inline fun <reified T : Any> SharedPreferences.get(key: String, defaultValue: T? = null): T? {
    val value = when (T::class) {
        String::class -> getString(key, defaultValue as? String) as T?
        Int::class -> getInt(key, defaultValue as? Int ?: -1) as T?
        Long::class -> getLong(key, defaultValue as? Long ?: -1L) as T?
        Float::class -> getFloat(key, defaultValue as? Float ?: -1f) as T?
        Boolean::class -> getBoolean(key, defaultValue as? Boolean ?: false) as T?
        else -> throw IllegalArgumentException("Unsupported type: ${T::class.java}")
    }
    return value
}

inline fun <reified T : Any> SharedPreferences.put(key: String, value: T?) {
    val editor = edit()
    when (T::class) {
        String::class -> editor.putString(key, value as? String)
        Int::class -> editor.putInt(key, value as? Int ?: -1)
        Long::class -> editor.putLong(key, value as? Long ?: -1L)
        Float::class -> editor.putFloat(key, value as? Float ?: -1f)
        Boolean::class -> editor.putBoolean(key, value as? Boolean ?: false)
        else -> throw IllegalArgumentException("Unsupported type: ${T::class.java}")
    }
    editor.apply()
}

fun parseDateWithTime(time: String?): String? {
    val inputPattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
    val outputPattern = "dd/MM/yyyy HH:mm:ss"
    val inputFormat = SimpleDateFormat(inputPattern, Locale.getDefault())
    val outputFormat = SimpleDateFormat(outputPattern, Locale.getDefault())
    val date: Date?
    var str: String? = null
    try {
        date = time?.let { inputFormat.parse(it) }
        str = date?.let { outputFormat.format(it) }
    } catch (e: ParseException) {
        e.printStackTrace()
    }
    return str
}

fun String.isValidEmail() = !TextUtils.isEmpty(this) && Patterns.EMAIL_ADDRESS.matcher(this).matches()

fun String?.isNullOrEmpty(): Boolean {
    return this == null || this.isEmpty()
}

fun String.toEditable(): Editable = Editable.Factory.getInstance().newEditable(this)

fun increaseFontSizeForPath(spannable: Spannable, path: String, increaseTime: Float) {
    val startIndexOfPath = spannable.toString().indexOf(path)
    spannable.setSpan(RelativeSizeSpan(increaseTime), startIndexOfPath,
        startIndexOfPath + path.length, 0)
}

fun reduceTextSizeAfterChar(text: String, afterChar: Char, reduceBy: Float): SpannableStringBuilder? {
    val smallSizeText = RelativeSizeSpan(reduceBy)
    val ssBuilder = SpannableStringBuilder(text)
    ssBuilder.setSpan(
        smallSizeText,
        text.indexOf(afterChar),
        text.length,
        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
    )
    return ssBuilder
}

fun String.appendString(append: () -> Any) = this + append()

fun Int.formatDecimalSeparator(): String {
    val amount =toString().reversed().chunked(3).joinToString(",").reversed()
    return "Rs. $amount"
}

fun Int.formatDecimalSeparatorWithoutRs(): String {
    val amount =toString().reversed().chunked(3).joinToString(",").reversed()
    return amount
}

fun Int.formatDecimalSeparatorWithSign(sign : String): String {
    val amount =toString().reversed().chunked(3).joinToString(",").reversed()
    return "Rs. $sign$amount"
}

fun passwordValidator(password : String) : String{
    if (password.isEmpty()) return "Please Enter Password"
    if (!password.contains("[a-zA-Z]".toRegex())) return "Password must contain alphabet"
    if (!password.any {it.isUpperCase()}) return "Password must contain 1 capital letter"
    if (!password.contains("[!\"#$%&'()*+,-./:;\\\\<=>?@\\[\\]^_`{|}~]".toRegex())) return "Password must contain special character"
    if (!password.any {it.isDigit()}) return "Password must contain number"
    if (password.length<8) return "Password length must be 8"
    return "Validate password"
}
