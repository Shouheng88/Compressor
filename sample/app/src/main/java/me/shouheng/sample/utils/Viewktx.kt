package me.shouheng.sample.utils

import androidx.appcompat.widget.AppCompatSpinner
import android.view.View
import android.widget.AdapterView

/** Quick method for spanner item select event. */
fun AppCompatSpinner.onItemSelected(
    onSelected: (position: Int) -> Unit
) {
    this.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) {
            /*noop*/
        }

        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            onSelected.invoke(position)
        }
    }
}

