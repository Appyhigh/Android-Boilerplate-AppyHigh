package com.appyhigh.utilityapp.utils

import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.LayerDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.appyhigh.utilityapp.R
import kotlinx.android.synthetic.main.dialog_rate.*


class RateDialog : DialogFragment() {


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_rate, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var stars = rating_bar.progressDrawable as LayerDrawable
        stars.getDrawable(2)
            .setColorFilter(resources.getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP)
        stars.getDrawable(0).setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_ATOP)
        stars.getDrawable(1)
            .setColorFilter(resources.getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP)

        rate_later.setOnClickListener { dismiss() }
        rate.setOnClickListener {
            val rating = rating_bar.rating
            if (rating >= 5.0) {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=" + activity!!.packageName)
                    )
                )
                dismiss()
            } else {
                dismiss()
                Toast.makeText(this.activity, "Rated Successfully!", Toast.LENGTH_SHORT).show()
            }
        }
    }

}
