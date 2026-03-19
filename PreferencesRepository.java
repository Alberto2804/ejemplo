package es.iesagora.proyectopetconnect;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

// Importamos el ViewBinding de tu fragment_app.xml
import es.iesagora.proyectopetconnect.databinding.FragmentAppBinding;

public class AppFragment extends Fragment {

    // Declaramos la variable de ViewBinding
    private FragmentAppBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inicializamos el ViewBinding
        binding = FragmentAppBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Ir a ajustes
        binding.btnSettings.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_appFragment_to_ajustesFragment)
        );

        /* Funcionalidad 1: Mi Mascota
        binding.cardMascota.setOnClickListener(v ->
                // Navigation.findNavController(v).navigate(R.id.action_appFragment_to_mascotaFragment)
        );

        // Funcionalidad 2: Agenda
        binding.cardAgenda.setOnClickListener(v ->
                // Navigation.findNavController(v).navigate(R.id.action_appFragment_to_agendaFragment)
        );

        // Funcionalidad 3: Lugares Cercanos
        binding.cardLugares.setOnClickListener(v ->
                // Navigation.findNavController(v).navigate(R.id.action_appFragment_to_lugaresFragment)
        );
        */
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Es vital anular el binding aquí para evitar fugas de memoria en Android
        binding = null;
    }
}