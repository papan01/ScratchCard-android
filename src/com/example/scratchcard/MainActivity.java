package com.example.scratchcard;

import com.example.scratchcard.view.ScratchCard;
import com.example.scratchcard.view.ScratchCard.OnScratchCardCompleteListener;
import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	ScratchCard mScratchCard;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mScratchCard = (ScratchCard) findViewById(R.id.scratchcard);
		mScratchCard.setOnScratchCardCompleteListener(new OnScratchCardCompleteListener() {
			
			@Override
			public void complete() {
				Toast.makeText(getApplicationContext(), "用戶已經刮的差不多了!", Toast.LENGTH_SHORT).show();
			}
		});
		
		mScratchCard.setText("HELLO!");
	}

}
