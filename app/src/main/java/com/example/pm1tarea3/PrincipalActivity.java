package com.example.pm1tarea3;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import com.example.pm1tarea3.databinding.ActivityPrincipalBinding;
import com.example.pm1tarea3.models.Persona;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.Console;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PrincipalActivity extends AppCompatActivity {


    ActivityPrincipalBinding binding;
    Uri imageUri;

    String url;
    StorageReference storageReference;
    ProgressDialog progressDialog;


    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    EditText nombre, apellido, fechaNacimiento;
    Spinner genero;

    Button btnGuardar;

    ImageView img;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPrincipalBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        binding.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                uploadImage();

            }
        });


        java.util.ArrayList<String> generos = new java.util.ArrayList<>();
        generos.add("Masculino");
        generos.add("Femenino");


        nombre=findViewById(R.id.editTextText2);
        apellido=findViewById(R.id.editTextText3);
        genero=findViewById(R.id.spinner);
        fechaNacimiento=findViewById(R.id.editTextDate);
        img=findViewById(R.id.imageView4);


        SpinnerAdapter spinnerAdapter = new ArrayAdapter<String>(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, generos);
        genero.setAdapter(spinnerAdapter);

        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });




        InicializarFirebase();

    }


    private void uploadImage() {

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Cargando Imagen....");
        progressDialog.show();


        SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US);
        Date now = new Date();
        String fileName = formatter.format(now);
        storageReference = FirebaseStorage.getInstance().getReference("images/"+fileName);


        storageReference.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        binding.imageView4.setImageURI(null);
                        Toast.makeText(PrincipalActivity.this,"Imagen cargada con exito",Toast.LENGTH_SHORT).show();
                        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Log.e("Uri",""+uri);
                                url=uri.toString();
                                crearPersona();

                            }
                        });




                        if (progressDialog.isShowing())
                            progressDialog.dismiss();

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {


                        if (progressDialog.isShowing())
                            progressDialog.dismiss();
                        Toast.makeText(PrincipalActivity.this,"Failed to Upload",Toast.LENGTH_SHORT).show();


                    }
                });

    }
    private void crearPersona(){
        String personKey=databaseReference.push().getKey();
        Persona p = new Persona();
        p.setNombres(nombre.getText().toString());
        p.setApellidos(apellido.getText().toString());
        p.setGenero(genero.getSelectedItem().toString());
        p.setImage(url);
        p.setFechaNacimiento(fechaNacimiento.getText().toString());
        databaseReference.child("Persona").child(personKey).setValue(p);

        Toast.makeText(this, "Agregado", Toast.LENGTH_LONG).show();

        Limpiar();
    }


    private void InicializarFirebase(){
        FirebaseApp.initializeApp(this);
        firebaseDatabase=FirebaseDatabase.getInstance();
        databaseReference=firebaseDatabase.getReference();
    }



    private void selectImage() {

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,100);

    }

    private void Limpiar(){
        nombre.setText("");
        apellido.setText("");
        genero.setSelection(0);
        fechaNacimiento.setText("");
        img.setImageResource(getResources().getIdentifier("hombre","drawable", this.getPackageName()));
        nombre.requestFocus();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && data != null && data.getData() != null){

            imageUri = data.getData();
            binding.imageView4.setImageURI(imageUri);


        }
    }
}