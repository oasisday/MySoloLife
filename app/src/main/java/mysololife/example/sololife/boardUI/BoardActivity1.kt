package mysololife.example.sololife.boardUI

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.mysololife.R
import mysololife.example.sololife.boardUI.adapter.TestAdapter

class BoardActivity1 : AppCompatActivity() {

    private val swipeRefreshLayout: SwipeRefreshLayout by lazy {
        findViewById(R.id.swipeRefreshLayout)
    }
    private val recyclerView: RecyclerView by lazy {
        findViewById(R.id.recyclerView)
    }

    private val adapter = TestAdapter();
    private val layoutManager = StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_board1)

        swipeRefreshLayout.setOnRefreshListener {
            swipeRefreshLayout.isRefreshing = false
            reload()
        }

        adapter.onLoadMore = {
            loadMore()
        }

        recyclerView.adapter = adapter
        recyclerView.layoutManager = layoutManager

        reload()
    }

    private fun reload() {
        recyclerView.post {
            adapter.reload(fetchDummyData(0, 20))
        }
    }

    private fun loadMore() {
        recyclerView.post {
            adapter.loadMore(fetchDummyData(adapter.itemCount, 20))
        }
    }

    private fun fetchDummyData(offset: Int, limit: Int): List<WaterfallItemModel> {

        val list = mutableListOf<WaterfallItemModel>()

        for(i in offset until offset + limit) {

            /*
            when ((1..10).random()) {

                1 -> {
                    list.add(WaterfallItemModel("Image 1", "https://i.pravatar.cc/150?img=1"))
                }

            }*/

            var r = (1..10).random()

            list.add(WaterfallItemModel("Image "+r, "https://i.pravatar.cc/150?img="+r))

        }

        return list
    }
}

data class WaterfallItemModel(
    val title: String,
    val imageURL: String
)