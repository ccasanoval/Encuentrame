package com.cesoft.encuentrame.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.firebase.client.Firebase;

////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar_Casanova on 17/02/2016.
////////////////////////////////////////////////////////////////////////////////////////////////////
public class Objeto implements Parcelable
{
	public static final String FIREBASE = "https://blazing-heat-3755.firebaseio.com/";
	public static final String NOMBRE = "objeto";

	public Objeto(){}

	//General
	protected String _id = null;
	public String getId(){return _id;}
	public void setId(String id){_id = id;}

	protected String nombre;
	protected String descripcion;
	public String getNombre(){return nombre;}
	public void setNombre(String v){nombre=v;}
	public String getDescripcion(){return descripcion;}
	public void setDescripcion(String v){descripcion=v;}

	//______________________________________________________________________________________________
	public String toString()
	{
		return "ID:"+getId()+", NOM:"+(nombre==null?"":nombre) + ", DESC:"+(descripcion==null?"":descripcion);
	}

	// PARCELABLE
	//______________________________________________________________________________________________
	protected Objeto(Parcel in)
	{
		setId(in.readString());
		setNombre(in.readString());
		setDescripcion(in.readString());
	}
	@Override
	public void writeToParcel(Parcel dest, int flags)
	{
		dest.writeString(getId());
		dest.writeString(getNombre());
		dest.writeString(getDescripcion());
	}
	@Override
	public int describeContents()
	{
		return 0;
	}
	public static final Parcelable.Creator<Objeto> CREATOR = new Parcelable.Creator<Objeto>()
	{
		@Override
		public Objeto createFromParcel(Parcel in)
		{
			return new Objeto(in);
		}
		@Override
		public Objeto[] newArray(int size)
		{
			return new Objeto[size];
		}
	};


	//FIREBASE
	//______________________________________________________________________________________________
/*	public void save(Firebase.CompletionListener listener)
	{
		Firebase ref = new Firebase(FIREBASE);
		Firebase objeto;
		if(_id == null)
			objeto = ref.child(NOMBRE).push();//.child(getId());
		else
			objeto = ref.child(NOMBRE).child(getId());
		objeto.setValue(this, listener);
	}
	/*new Firebase.CompletionListener()
    {
        @Override
        public void onComplete(FirebaseError error, Firebase firebase)
        {
            if(error != null)
                System.out.println("Data could not be saved. " + error.getMessage());
            else
                System.out.println("Data saved successfully.");
        }
    });*/

	/*public static Objeto findById(String id)
	{
	}*/
}

/*
    Firebase.getDefaultConfig().setPersistenceEnabled(true);


    Firebase scoresRef = new Firebase("https://dinosaur-facts.firebaseio.com/scores");
    scoresRef.keepSynced(true);

---------
	alansRef.child("nombre").setValue("Alan Turing");
	alansRef.child("descripcion").setValue("1912");

	Firebase usersRef = ref.child("users");
    Map<String, String> alanisawesomeMap = new HashMap<String, String>();
    alanisawesomeMap.put("birthYear", "1912");
    alanisawesomeMap.put("fullName", "Alan Turing");
    Map<String, Map<String, String>> users = new HashMap<String, Map<String, String>>();
    users.put("alanisawesome", alanisawesomeMap);
    usersRef.setValue(users);

	Firebase alanRef = usersRef.child("alanisawesome");
    Map<String, Object> nickname = new HashMap<String, Object>();
    nickname.put("nickname", "Alan The Machine");
    alanRef.updateChildren(nickname);

	passing null in a map will delete the data at that location in the database.

    Map<String, Object> nicknames = new HashMap<String, Object>();
    nickname.put("alanisawesome/nickname", "Alan The Machine");
    nickname.put("gracehop/nickname", "Amazing Grace");
    usersRef.updateChildren(nicknames);


    ref.setValue("I'm writing data", new Firebase.CompletionListener()
    {
        @Override
        public void onComplete(FirebaseError firebaseError, Firebase firebase)
        {
            if (firebaseError != null)
                System.out.println("Data could not be saved. " + firebaseError.getMessage());
            else
                System.out.println("Data saved successfully.");
        }
    });


    Firebase postRef = ref.child("posts");
    Map<String, String> post1 = new HashMap<String, String>();
    post1.put("author", "gracehop");
    post1.put("title", "Announcing COBOL, a New Programming Language");
    postRef.push().setValue(post1);
    Map<String, String> post2 = new HashMap<String, String>();
    post2.put("author", "alanisawesome");
    post2.put("title", "The Turing Machine");
    postRef.push().setValue(post2);


    // Generate a reference to a new location and add some data using push()
    Firebase postRef = ref.child("posts");
    Firebase newPostRef = postRef.push();
    // Add some data to the new location
    Map<String, String> post1 = new HashMap<String, String>();
    post1.put("author", "gracehop");
    post1.put("title", "Announcing COBOL, a New Programming Language");
    newPostRef.setValue(post1);
    // Get the unique ID generated by push()
    String postId = newPostRef.getKey();


    Firebase upvotesRef = new Firebase("https://docs-examples.firebaseio.com/android/saving-data/fireblog/posts/-JRHTHaIs-jNPLXOQivY/upvotes");
    upvotesRef.runTransaction(new Transaction.Handler() {
        @Override
        public Transaction.Result doTransaction(MutableData currentData) {
            if(currentData.getValue() == null) {
                currentData.setValue(1);
            } else {
                currentData.setValue((Long) currentData.getValue() + 1);
            }
            return Transaction.success(currentData); //we can also abort by calling Transaction.abort()
        }
        @Override
        public void onComplete(FirebaseError firebaseError, boolean committed, DataSnapshot currentData) {
            //This method will be called once with the results of the transaction.
        }
    });


        final CountDownLatch done = new CountDownLatch(1);
    ref.setValue("SOME DATA", new Firebase.CompletionListener() {
        @Override
        public void onComplete(FirebaseError firebaseError, Firebase firebase) {
            System.out.println("done");
            done.countDown();
        }
    });
    done.await();




    ----- SECURITY
    ".read": "auth !== null",

      {
      "rules": {
        "users": {
          "$user_id": {
            // grants write access to the owner of this user account
            // whose uid must exactly match the key ($user_id)
            ".write": "$user_id === auth.uid"
          }
        }
      }
    }


    ---- READ

  Firebase ref = new Firebase("https://docs-examples.firebaseio.com/web/saving-data/fireblog/posts");
  // Attach an listener to read the data at our posts reference
  ref.addValueEventListener(new ValueEventListener() {
      @Override
      public void onDataChange(DataSnapshot snapshot)
      {
          System.out.println("There are " + snapshot.getChildrenCount() + " blog posts");
          for (DataSnapshot postSnapshot: snapshot.getChildren())
          {
            BlogPost post = postSnapshot.getValue(BlogPost.class);
            System.out.println(post.getAuthor() + " - " + post.getTitle());
          }
      }
      @Override
      public void onCancelled(FirebaseError firebaseError) {
          System.out.println("The read failed: " + firebaseError.getMessage());
      }
  });


  Firebase ref = new Firebase("https://docs-examples.firebaseio.com/web/saving-data/fireblog/posts");
ref.addChildEventListener(new ChildEventListener() {
    // Retrieve new posts as they are added to the database
    @Override
    public void onChildAdded(DataSnapshot snapshot, String previousChildKey) {
    //todo:check that its not null
        BlogPost newPost = snapshot.getValue(BlogPost.class);
        System.out.println("Author: " + newPost.getAuthor());
        System.out.println("Title: " + newPost.getTitle());
    }
    //... ChildEventListener also defines onChildChanged, onChildRemoved,
    //    onChildMoved and onCanceled, covered in later sections.
});


// Get the data on a post that has changed
@Override
public void onChildChanged(DataSnapshot snapshot, String previousChildKey)
 {
    String title = (String) snapshot.child("title").getValue();
    System.out.println("The updated post title is " + title);
}

// Get the data on a post that has been removed
@Override
public void onChildRemoved(DataSnapshot snapshot) {
    String title = (String) snapshot.child("title").getValue();
    System.out.println("The blog post titled " + title + " has been deleted");
}



    ref.removeEventListener(originalListener);


    -- Una vez

        .child(NOMBRE)
    });


    orderByChild(), orderByKey(), orderByValue(), or orderByPriority().
    limitToFirst(), limitToLast(), startAt(), endAt(), and equalTo()


        Firebase ref = new Firebase("https://dinosaur-facts.firebaseio.com/dinosaurs");
    Query queryRef = ref.orderByChild("dimensions/height");
    queryRef.addChildEventListener(new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot snapshot, String previousChild) {
            DinosaurFacts facts = snapshot.getValue(DinosaurFacts.class);
            System.out.println(snapshot.getKey() + " was " + facts.getHeight() + " meters tall");
        }
        // ....
    });


    orderByChild() ============================> .indexOn  / key and and priority are indexed automatically
        {
      "rules": {
        "dinosaurs": {
          ".indexOn": ["height", "length"]
        }
      }
    }


        {
      "rules": {
        "scores": {
          ".indexOn": ".value"
        }
      }
    }
        Firebase scoresRef = new Firebase("https://dinosaur-facts.firebaseio.com/scores");
    Query queryRef = scoresRef.orderByValue();
    queryRef.addChildEventListener(new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot snapshot, String previousChildKey) {
          System.out.println("The " + snapshot.getKey() + " dinosaur's score is " + snapshot.getValue());
        }
        // ....
    });

        Firebase ref = new Firebase("https://dinosaur-facts.firebaseio.com/dinosaurs");
    Query queryRef = ref.orderByKey();
    queryRef.addChildEventListener(new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot snapshot, String previousChild) {
            System.out.println(snapshot.getKey());
        }
        // ....
    });





--------------------
Firebase connectedRef = new Firebase("https://<YOUR-FIREBASE-APP>.firebaseio.com/.info/connected");
connectedRef.addValueEventListener(new ValueEventListener() {
  @Override
  public void onDataChange(DataSnapshot snapshot) {
    boolean connected = snapshot.getValue(Boolean.class);
    if (connected) {
      System.out.println("connected");
    } else {
      System.out.println("not connected");
    }
  }
  @Override
  public void onCancelled(FirebaseError error) {
    System.err.println("Listener was cancelled");
  }
})



    Firebase userLastOnlineRef = new Firebase("https://<YOUR-FIREBASE-APP>.firebaseio.com/users/joe/lastOnline");
    userLastOnlineRef.onDisconnect().setValue(ServerValue.TIMESTAMP);
*/