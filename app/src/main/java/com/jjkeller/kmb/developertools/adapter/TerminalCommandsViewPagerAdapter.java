package com.jjkeller.kmb.developertools.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter to control fragments pager of a TabLayout.
 */

public class TerminalCommandsViewPagerAdapter extends FragmentPagerAdapter {
	private final List<Fragment> mFragmentList = new ArrayList<>();
	private final List<String> mFragmentTitleList = new ArrayList<>();

	public TerminalCommandsViewPagerAdapter(FragmentManager manager) {
		super(manager);
	}

	@Override
	public Fragment getItem(int position) {
		return mFragmentList.get(position);
	}

	@Override
	public int getCount() {
		return mFragmentList.size();
	}

	public void addFragment(Fragment fragment, String title) {
		mFragmentList.add(fragment);
		mFragmentTitleList.add(title);
	}

	@Override
	public CharSequence getPageTitle(int position) {
		return mFragmentTitleList.get(position);
	}

	/**
	 * Need to 'replace' fragment on orientation change.
	 * https://medium.com/@roideuniverse/android-viewpager-fragmentpageradapter-and-orientation-changes-256c23bee035
	 */
	@Override
	public Object instantiateItem(ViewGroup container, int position)
	{
		Object ret = super.instantiateItem(container, position);

		if (mFragmentList.size() > 0){
			mFragmentList.remove(position);
		}
		mFragmentList.add(position, (Fragment) ret);

		return ret;
	}
}
