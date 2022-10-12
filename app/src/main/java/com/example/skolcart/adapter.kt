package com.example.skolcart

import android.media.MediaPlayer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.VideoView
import androidx.recyclerview.widget.RecyclerView

class adapter(var list : ArrayList<String>) : RecyclerView.Adapter<adapter.viewmodel>() {

    class viewmodel(itemView: View) : RecyclerView.ViewHolder(itemView){
        var vi = itemView.findViewById<VideoView>(R.id.viedeoview)
        var pro = itemView.findViewById<ProgressBar>(R.id.progressbar)
        fun setvideo(url : String)
        {
            vi.setVideoPath(url)
            vi.setOnPreparedListener(object  : MediaPlayer.OnPreparedListener {
                override fun onPrepared(mp: MediaPlayer?) {
                    pro.visibility = View.GONE
                    mp!!.start()
                    val videoration = mp.videoWidth.toFloat()/mp.videoHeight.toFloat()
                    val screenration = vi.width.toFloat() / vi.height.toFloat()

                    val scale = videoration/screenration
                    if(scale > 1f)
                    {
                        vi.scaleX = scale
                    }
                    else
                    {
                        vi.scaleY = (1f / scale)
                    }


                }

            })
            vi.setOnCompletionListener(object  : MediaPlayer.OnCompletionListener {
                override fun onCompletion(mp: MediaPlayer?) {
                    mp!!.start()
                }
            })

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewmodel {
        return viewmodel(LayoutInflater.from(parent.context).inflate(R.layout.video_item,parent,false))
    }

    override fun onBindViewHolder(holder: viewmodel, position: Int) {
        holder.setvideo(list.get(position))
    }

    override fun getItemCount(): Int {
        return list.size
    }


}