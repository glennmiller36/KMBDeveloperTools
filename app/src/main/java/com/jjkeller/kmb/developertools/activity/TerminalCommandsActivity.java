package com.jjkeller.kmb.developertools.activity;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.jjkeller.kmb.developertools.R;
import com.jjkeller.kmb.developertools.adapter.TerminalCommandsViewPagerAdapter;
import com.jjkeller.kmb.developertools.fragment.TerminalCommandsFavoritesFragment;
import com.jjkeller.kmb.developertools.fragment.TerminalCommandsAllFragment;
import com.jjkeller.kmb.developertools.manager.Services;
import com.jjkeller.kmb.developertools.model.TerminalCommandModel;

/**
 * Activity to display list of available commands and corresponding command description.
 */
public class TerminalCommandsActivity extends AppCompatActivity {

	private ViewPager mViewPager;
	private TabLayout mTabLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(Services.Theme().getThemeResourceId());
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_terminal_commands);

		setupViews();
	}

	/**
	 * Define handles to the child views.
	 */
	private void setupViews() {
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		mViewPager = (ViewPager) findViewById(R.id.viewpager);
		setupViewPager(mViewPager);

		mTabLayout = (TabLayout) findViewById(R.id.tabs);
		mTabLayout.setupWithViewPager(mViewPager);
	}

	private void setupViewPager(ViewPager viewPager) {
		TerminalCommandsViewPagerAdapter adapter = new TerminalCommandsViewPagerAdapter(getSupportFragmentManager());
		adapter.addFragment(new TerminalCommandsFavoritesFragment(), getString(R.string.favorites));
		adapter.addFragment(new TerminalCommandsAllFragment(), getString(R.string.all_commands));
		viewPager.setAdapter(adapter);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	public void onIsFavoriteCheckChanged(TerminalCommandModel model) {
		TerminalCommandsFavoritesFragment f = (TerminalCommandsFavoritesFragment) ((TerminalCommandsViewPagerAdapter) mViewPager.getAdapter()).getItem(0);
		f.onFavoriteChanged(model);
	}
}
