package es.iesagora.proyectopetconnect;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import api.Resource;
import es.iesagora.proyectopetconnect.databinding.FragmentAjustesBinding;
import viewmodel.UserViewModel;
import androidx.appcompat.app.AppCompatDelegate;

public class AjustesFragment extends Fragment {

    private FragmentAjustesBinding binding;
    private UserViewModel userViewModel;

    private ActivityResultLauncher<String> selectorImagenLauncher;
    private Uri nuevaFotoUri = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        selectorImagenLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        nuevaFotoUri = uri;
                        binding.ivFotoPerfil.setImageURI(uri);
                    }
                }
        );
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAjustesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicializar el ViewModel
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        boolean modoOscuroActivado = userViewModel.isModoOscuro();
        binding.switchModoOscuro.setChecked(modoOscuroActivado);

        // 2. Escuchar cuando el usuario toca el interruptor
        binding.switchModoOscuro.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Guardamos su preferencia
            userViewModel.setModoOscuro(isChecked);

            // Aplicamos el tema a toda la app al instante
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });
        // Configurar botones
        binding.btnCambiarFoto.setOnClickListener(v -> selectorImagenLauncher.launch("image/*"));
        binding.btnGuardarPerfil.setOnClickListener(v -> guardarCambios());
        binding.btnLogout.setOnClickListener(v -> hacerLogout());
    }

    private void guardarCambios() {
        String nuevoNombre = binding.etNombreUsuario.getText().toString().trim();

        if (nuevoNombre.isEmpty()) {
            Toast.makeText(requireContext(), "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.btnGuardarPerfil.setEnabled(false);
        binding.btnGuardarPerfil.setText("Guardando...");

        File archivoFoto = (nuevaFotoUri != null) ? uriToFile(nuevaFotoUri) : null;

        // Llamamos al ViewModel y OBSERVMOS (Observer Pattern) la respuesta
        userViewModel.actualizarPerfil(nuevoNombre, archivoFoto).observe(getViewLifecycleOwner(), resource -> {
            if (resource != null) {
                switch (resource.status) {
                    case LOADING:
                        // Podrías mostrar un ProgressBar aquí
                        break;
                    case SUCCESS:
                        if (archivoFoto != null) {
                            // Si se subió la foto con éxito, ahora disparamos la actualización del nombre
                            actualizarNombrePostFoto(nuevoNombre);
                            nuevaFotoUri = null; // Limpiamos
                        } else {
                            Toast.makeText(getContext(), "Perfil actualizado", Toast.LENGTH_SHORT).show();
                            restaurarBoton();
                        }
                        break;
                    case ERROR:
                        Toast.makeText(getContext(), resource.message, Toast.LENGTH_SHORT).show();
                        restaurarBoton();
                        break;
                }
            }
        });
    }

    private void actualizarNombrePostFoto(String nuevoNombre) {
        userViewModel.actualizarSoloNombre(nuevoNombre).observe(getViewLifecycleOwner(), resource -> {
            if (resource != null && resource.status == Resource.Status.SUCCESS) {
                Toast.makeText(getContext(), "Foto y nombre actualizados", Toast.LENGTH_SHORT).show();
                restaurarBoton();
            } else if (resource != null && resource.status == Resource.Status.ERROR) {
                Toast.makeText(getContext(), "Foto subida, pero error en nombre: " + resource.message, Toast.LENGTH_SHORT).show();
                restaurarBoton();
            }
        });
    }

    private void hacerLogout() {
        userViewModel.hacerLogout(); // El ViewModel limpia las SharedPreferences
        Navigation.findNavController(requireView()).navigate(
                R.id.loginFragment,
                null,
                new NavOptions.Builder().setPopUpTo(R.id.nav_graph, true).build()
        );
    }

    private File uriToFile(Uri uri) {
        try {
            InputStream in = requireContext().getContentResolver().openInputStream(uri);
            File tempFile = File.createTempFile("avatar_tmp", ".jpg", requireContext().getCacheDir());
            FileOutputStream out = new FileOutputStream(tempFile);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) { out.write(buffer, 0, read); }
            out.close();
            in.close();
            return tempFile;
        } catch (Exception e) {
            return null;
        }
    }

    private void restaurarBoton() {
        binding.btnGuardarPerfil.setEnabled(true);
        binding.btnGuardarPerfil.setText("Guardar Cambios");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}