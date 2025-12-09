package com.example.finalproj.services;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.finalproj.model.Item;
import com.example.finalproj.model.ItemCart;
import com.example.finalproj.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.UnaryOperator;


public class DatabaseService {

    private static final String TAG = "DatabaseService";

    // Firebase root paths
    private static final String USERS_PATH = "users";
    private static final String ITEMS_PATH = "items";
    private static final String CARTS_PATH = "carts";

    // Singleton instance
    private static DatabaseService instance;
    private FirebaseAuth mAuth;



    // Firebase Database reference
    private final DatabaseReference databaseReference;

    private DatabaseService() {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
    }

    public static DatabaseService getInstance() {
        if (instance == null) {
            instance = new DatabaseService();
        }
        return instance;
    }

    // region Generic Database Helpers

    private DatabaseReference readData(@NotNull final String path) {
        return databaseReference.child(path);
    }

    private void writeData(@NotNull final String path, @NotNull final Object data, @Nullable final DatabaseCallback<Void> callback) {
        readData(path).setValue(data, (error, ref) -> {
            if (error != null) {
                if (callback != null) callback.onFailed(error.toException());
            } else {
                if (callback != null) callback.onCompleted(null);
            }
        });
    }

    private void deleteData(@NotNull final String path, @Nullable final DatabaseCallback<Void> callback) {
        readData(path).removeValue((error, ref) -> {
            if (error != null) {
                if (callback != null) callback.onFailed(error.toException());
            } else {
                if (callback != null) callback.onCompleted(null);
            }
        });
    }

    private <T> void getData(@NotNull final String path, @NotNull final Class<T> clazz, @NotNull final DatabaseCallback<T> callback) {
        readData(path).get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e(TAG, "Error getting data", task.getException());
                callback.onFailed(task.getException());
                return;
            }
            T data = task.getResult().getValue(clazz);
            callback.onCompleted(data);
        });
    }

    private <T> void getDataList(@NotNull final String path, @NotNull final Class<T> clazz, @NotNull final DatabaseCallback<List<T>> callback) {
        readData(path).get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e(TAG, "Error getting data list", task.getException());
                callback.onFailed(task.getException());
                return;
            }
            List<T> list = new ArrayList<>();
            for (DataSnapshot snapshot : task.getResult().getChildren()) {
                T item = snapshot.getValue(clazz);
                if (item != null) list.add(item);
            }
            callback.onCompleted(list);
        });
    }

    private String generateNewId(@NotNull final String path) {
        return databaseReference.child(path).push().getKey();
    }

    private <T> void runTransaction(@NotNull final String path, @NotNull final Class<T> clazz, @NotNull UnaryOperator<T> function, @NotNull final DatabaseCallback<T> callback) {
        readData(path).runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                T currentValue = currentData.getValue(clazz);
                currentValue = function.apply(currentValue);
                currentData.setValue(currentValue);
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                if (error != null) {
                    callback.onFailed(error.toException());
                    return;
                }
                T result = currentData != null ? currentData.getValue(clazz) : null;
                callback.onCompleted(result);
            }
        });
    }

    // endregion

    // region Callback Interface
    public interface DatabaseCallback<T> {
        void onCompleted(T object);
        void onFailed(Exception e);
    }
    // endregion


    // region User Section

    public String generateUserId() {
        return generateNewId(USERS_PATH);
    }

    public void createNewUser(@NotNull final User user, @Nullable final DatabaseCallback<Void> callback) {
        writeData(USERS_PATH + "/" + user.getId(), user, callback);
    }

    /// Login with email and password
    /// @param email , password
    /// @param callback the callback to call when the operation is completed
    ///              the callback will receive String (user id)
    ///            if the operation fails, the callback will receive an exception
    /// @see DatabaseCallback
    /// @see FirebaseAuth

    public void LoginUser(@NotNull final String email,final String password,
                          @Nullable final DatabaseCallback<String> callback) {

        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        mAuth.signInWithEmailAndPassword(email,password)

                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {
                        Log.d("TAG", "createUserWithEmail:success");

                        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        callback.onCompleted(uid);

                    } else {
                        Log.w("TAG", "createUserWithEmail:failure", task.getException());

                        if (callback != null)
                            callback.onFailed(task.getException());
                    }
                });
    }

    public void getUser(@NotNull final String uid, @NotNull final DatabaseCallback<User> callback) {
        getData(USERS_PATH + "/" + uid, User.class, callback);
    }

    public void getUserList(@NotNull final DatabaseCallback<List<User>> callback) {
        getDataList(USERS_PATH, User.class, callback);
    }

    public void deleteUser(@NotNull final String uid, @Nullable final DatabaseCallback<Void> callback) {
        deleteData(USERS_PATH + "/" + uid, callback);
    }

    public void updateUser(@NotNull final User user, @Nullable final DatabaseCallback<Void> callback) {
        runTransaction(USERS_PATH + "/" + user.getId(), User.class, old -> user, new DatabaseCallback<User>() {
            @Override
            public void onCompleted(User object) {
                if (callback != null) callback.onCompleted(null);
            }

            @Override
            public void onFailed(Exception e) {
                if (callback != null) callback.onFailed(e);
            }
        });
    }

    public void getUserByEmailAndPassword(@NotNull final String email, @NotNull final String password, @NotNull final DatabaseCallback<User> callback) {
        readData(USERS_PATH).orderByChild("email").equalTo(email).get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                callback.onFailed(task.getException());
                return;
            }

            if (task.getResult().getChildrenCount() == 0) {
                callback.onFailed(new Exception("User not found"));
                return;
            }

            for (DataSnapshot snapshot : task.getResult().getChildren()) {
                User user = snapshot.getValue(User.class);
                if (user != null && Objects.equals(user.getPassword(), password)) {
                    callback.onCompleted(user);
                    return;
                }
            }
            callback.onFailed(new Exception("Invalid email or password"));
        });
    }

    public void checkIfEmailExists(@NotNull final String email, @NotNull final DatabaseCallback<Boolean> callback) {
        readData(USERS_PATH).orderByChild("email").equalTo(email).get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                callback.onFailed(task.getException());
                return;
            }
            boolean exists = task.getResult().getChildrenCount() > 0;
            callback.onCompleted(exists);
        });
    }

    // endregion


    // region Items Section

    public String generateItemId() {
        return generateNewId(ITEMS_PATH);
    }

    public void createNewItem(@NotNull final Item item, @Nullable final DatabaseCallback<Void> callback) {
        writeData(ITEMS_PATH + "/" + item.getId(), item, callback);
    }

    public void getItem(@NotNull final String itemId, @NotNull final DatabaseCallback<Item> callback) {
        getData(ITEMS_PATH + "/" + itemId, Item.class, callback);
    }

    public void getItemList(@NotNull final DatabaseCallback<List<Item>> callback) {
        getDataList(ITEMS_PATH, Item.class, callback);
    }

    public void deleteItem(@NotNull final String itemId, @Nullable final DatabaseCallback<Void> callback) {
        deleteData(ITEMS_PATH + "/" + itemId, callback);
    }

    // endregion


    // region Cart Section

    public String generateCartId() {
        return generateNewId(CARTS_PATH);
    }

    public void createNewCart(@NotNull final ItemCart cart, @Nullable final DatabaseCallback<Void> callback) {
        writeData(CARTS_PATH + "/" + cart.getId(), cart, callback);
    }

    public void getCart(@NotNull final String cartId, @NotNull final DatabaseCallback<ItemCart> callback) {
        getData(CARTS_PATH + "/" + cartId, ItemCart.class, callback);
    }

    public void getCartList(@NotNull final DatabaseCallback<List<ItemCart>> callback) {
        getDataList(CARTS_PATH, ItemCart.class, callback);
    }

    public void deleteCart(@NotNull final String cartId, @Nullable final DatabaseCallback<Void> callback) {
        deleteData(CARTS_PATH + "/" + cartId, callback);
    }


    // endregion
}
