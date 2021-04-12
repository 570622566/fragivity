@file:JvmName("ReportHelper")

package androidx.fragment.app

import android.os.Parcelable
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.NavHostFragment
import com.github.fragivity.util.appendBackground

private class ReportFragmentManager : FragmentManager() {
    override fun saveAllState(): Parcelable {
        val fragments = fragments
        if (fragments.size > 1) {
            var fragment: Fragment
            for (i in 0..fragments.size - 2) {
                fragment = fragments[i]
                if (fragment.mMaxState != Lifecycle.State.CREATED) {
                    fragment.mMaxState = Lifecycle.State.CREATED
                }
            }
        }
        return super.saveAllState()
    }
}

private class BackgroundFragmentManager(
    private val selfFragment: Fragment
) : FragmentManager() {
    override fun dispatchViewCreated() {
        super.dispatchViewCreated()
        val rootView = selfFragment.view
        if (rootView != null && rootView.background == null) {
            rootView.appendBackground()
        }
    }
}

private class FragmentFactoryProxy(
    private val factory: FragmentFactory
) : FragmentFactory() {
    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
        val fragment = factory.instantiate(classLoader, className)
        if (fragment is NavHostFragment) {
            fragment.setupFragmentManager(ReportFragmentManager())
        } else {
            fragment.setupFragmentManager(BackgroundFragmentManager(fragment))
        }
        return fragment
    }
}

private fun Fragment.setupFragmentManager(manager: FragmentManager) {
    mChildFragmentManager = manager
}

fun FragmentManager.proxyFragmentFactory() {
    val oldFactory = fragmentFactory
    if (oldFactory !is FragmentFactoryProxy) {
        fragmentFactory = FragmentFactoryProxy(oldFactory)
    }
}

fun FragmentActivity.proxyFragmentFactory() {
    supportFragmentManager.proxyFragmentFactory()
}