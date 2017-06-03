package samuelpalmer.common.contentprovidersharedpreferences;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

class Remote {

	private final String preferencesName;
	private final String authority;
	private final ContentResolver contentResolver;

	public Remote(Context context, String authority, String preferencesName) {
		this.preferencesName = preferencesName;
		this.authority = authority;
		contentResolver = context.getContentResolver();
	}

	public <TArgs, TResult> TResult process(Contract<TArgs, TResult> contract, TArgs args) {
		Bundle resultWrapper = contentResolver.call(
			new Uri.Builder()
				.scheme("content")
				.authority(authority)
				.build(),
			contract.methodName(),
			preferencesName,
			contract.serialiseArgs(args)
		);

		if (resultWrapper != null) { // I'm pretty sure I've seen this set to null when the content provider crashed.
			Bundle serialisedResult = resultWrapper.getBundle(null);
			if (serialisedResult != null) // Sometimes when the content provider crashes, it returns an empty bundle.
				return contract.deserialiseResult(serialisedResult);
		}
		
		throw new RuntimeException("Didn't get a result. The content provider probably crashed.");
	}

}
