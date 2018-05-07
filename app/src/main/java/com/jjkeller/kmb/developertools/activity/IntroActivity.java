package com.jjkeller.kmb.developertools.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.jjkeller.kmb.developertools.R;

import agency.tango.materialintroscreen.MaterialIntroActivity;
import agency.tango.materialintroscreen.MessageButtonBehaviour;
import agency.tango.materialintroscreen.SlideFragmentBuilder;

public class IntroActivity extends MaterialIntroActivity {
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		IntroActivity self = this;

		addSlide(new SlideFragmentBuilder()
				.backgroundColor(R.color.material_blue_500)
				.buttonsColor(R.color.material_indigo_500)
				.image(R.drawable.intro_devtools)
				.title(getString(R.string.intro_devtools_title))
				.description(getString(R.string.intro_devtools_description))
				.build());

		addSlide(new SlideFragmentBuilder()
				.backgroundColor(R.color.material_blue_500)
				.buttonsColor(R.color.material_indigo_500)
				.image(R.drawable.intro_sqlquery)
				.title(getString(R.string.intro_sqlqueries_title))
				.description(getString(R.string.intro_sqlqueries_description))
				.build());

		addSlide(new SlideFragmentBuilder()
				.backgroundColor(R.color.material_blue_500)
				.buttonsColor(R.color.material_indigo_500)
				.image(R.drawable.intro_backup)
				.title(getString(R.string.intro_sqlitemanagement_title))
				.description(getString(R.string.intro_sqlitemanagement_description))
				.build());

		addSlide(new SlideFragmentBuilder()
				.backgroundColor(R.color.material_blue_500)
				.buttonsColor(R.color.material_indigo_500)
				.image(R.drawable.intro_bluetooth)
				.title(getString(R.string.intro_bluetoothterminal_title))
				.description(getString(R.string.intro_bluetoothterminal_description))
				.build());

		addSlide(new SlideFragmentBuilder()
				.backgroundColor(R.color.material_blue_500)
				.buttonsColor(R.color.material_indigo_500)
				.image(R.drawable.intro_email)
				.title(getString(R.string.intro_diagnostics_title))
				.description(getString(R.string.intro_diagnostics_description))
				.build());

		addSlide(new SlideFragmentBuilder()
				.backgroundColor(R.color.material_blue_500)
				.buttonsColor(R.color.material_indigo_500)
				.image(R.drawable.intro_theming)
				.title(getString(R.string.intro_theming_title))
				.description(getString(R.string.intro_theming_description))
						.build(),
				new MessageButtonBehaviour(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						self.finish();
					}
				}, getString(R.string.got_it)));
	}
}
