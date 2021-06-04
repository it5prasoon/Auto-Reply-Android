package com.matrix.autoreply.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.Nullable
import androidx.fragment.app.Fragment
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.matrix.autoreply.R

class SingleFriendFragment : Fragment() {

    var hello: TextView? = null

    @Nullable
    override fun onCreateView(inflater: LayoutInflater, @Nullable container: ViewGroup?, @Nullable savedInstanceState: Bundle?): View {
        val view: View = inflater.inflate(R.layout.fragment_single_friend, container, false)
        hello = view.findViewById(R.id.hello)

        // this will start python
        if (! Python.isStarted()) {
            Python.start(AndroidPlatform(requireContext()));
        }

        // now create python instances
        val py: Python = Python.getInstance()

        // now create python object
        val pyobj: PyObject = py.getModule("helloWorld")

        // now call this function
        val obj: PyObject = pyobj.callAttr("main")

        // now set return text to textview
        hello?.text = obj.toString()


        return view
    }
}