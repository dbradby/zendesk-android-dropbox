How to integrate Zendesk into your Android Application

- put the zendesk.jar into a "lib" folder underneath the root directory of your Android project


- Ensure the application has permission to access the network by adding the following to your manifest

	<uses-permission android:name="android.permission.INTERNET"></uses-permission> 


- Configure the endpoint (the domain you use to log into zendesk) in the manifest, replacing "yoursubdomain" in the text below

    <meta-data android:name="zendesk_url" android:value="yoursubdomain.zendesk.com"/>


- Configure the title used in the helpdesk dialog

    <meta-data android:name="zendesk_title" android:value="Your Title"/>	


- Invoke the builder and create the dialog by inserting the following code (usually in your onCreateDialog method of your Activity).
	
	ZendeskDialog.Builder(this).create();


- Optionally you can override the default description text in the dialog from within the manifest

    <meta-data android:name="zendesk_description" android:value="How may we help you?"/>


- Optionally you can configure everything programatically using the Dialog Builder style API

    ZendeskDialog.Builder(this)
        .setTitle("custom title")
        .setDescription("custom description")
        .setUrl("subdomain.zendesk.com")
        .create();


