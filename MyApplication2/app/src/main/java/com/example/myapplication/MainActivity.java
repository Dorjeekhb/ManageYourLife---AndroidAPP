package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Debug;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.OvershootInterpolator;
import android.widget.ArrayAdapter;
import android.widget.CalendarView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerTareas;
    private FloatingActionButton fabAgregar, fabFilter;
    private List<Tarea> listaTareas = new ArrayList<>();
    private List<Tarea> allTasks = new ArrayList<>(); // Lista original para restaurar el filtro
    private TareaAdapter adaptador;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicialización de vistas
        recyclerTareas = findViewById(R.id.recyclerTareas);
        fabAgregar = findViewById(R.id.fabAgregar);
        fabFilter = findViewById(R.id.fabFilter);

        // Configuración del RecyclerView (Punto 3)
        recyclerTareas.setLayoutManager(new LinearLayoutManager(this));
        recyclerTareas.addItemDecoration(new RecyclerView.ItemDecoration() {
            public void getItemOffsets(@NonNull Rect outRect,
                                       @NonNull View view,
                                       @NonNull RecyclerView parent,
                                       @NonNull RecyclerView.State state) {
                int position = parent.getChildAdapterPosition(view);
                // Reducir el espacio entre elementos
                outRect.set(position * 2, position * 3, 0, 0);
            }
        });
        recyclerTareas.setAdapter(adaptador);

        // Modo oscuro/claro
        SharedPreferences themePrefs = getSharedPreferences("ThemePrefs", MODE_PRIVATE);
        boolean isDarkMode = themePrefs.getBoolean("darkMode", true);
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        checkNotificationPermission();



        // Cargar tareas
        loadTasks();
        allTasks = new ArrayList<>(listaTareas);

        // Inicializar vistas
        recyclerTareas = findViewById(R.id.recyclerTareas);
        fabAgregar = findViewById(R.id.fabAgregar);
        fabFilter = findViewById(R.id.fabFilter);

        recyclerTareas.setLayoutManager(new LinearLayoutManager(this));
        recyclerTareas.setItemAnimator(new DefaultItemAnimator());
        // Configuración del adaptador
        // Al crear el adaptador
        adaptador = new TareaAdapter(listaTareas, 0, 0f);
        adaptador.setOnTaskClickListener(new TareaAdapter.OnTaskClickListener() {
            @Override
            public void onItemClick(Tarea tarea, int position) {
                // Animación normal de la tarjeta
            }

            @Override
            public void onDetailsClick(Tarea tarea, int position) {
                mostrarDialogoEditarTarea(tarea, position); // Tu método de edición
            }
        });
        recyclerTareas.setAdapter(adaptador);

        setupSwipeToDelete();

        // FAB Agregar
        fabAgregar.setOnClickListener(view -> {
            view.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.fab_bounce));
            mostrarDialogoAgregarTarea();
        });

        // FAB Filtrar (restablece lista original)
        fabFilter.setOnClickListener(v -> {
            v.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.fab_bounce));
            mostrarDialogoFiltrarTareas(); // Nuevo método para mostrar opciones de filtrado
        });
    }

    private void mostrarDialogoFiltrarTareas() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_filter, null);
        builder.setView(dialogView);

        Spinner spinnerPriority = dialogView.findViewById(R.id.spinnerFilterPriority);
        EditText etLabel = dialogView.findViewById(R.id.etFilterLabel);
        CalendarView calendarView = dialogView.findViewById(R.id.calendarView);

        // Configurar spinner
        ArrayAdapter<Priority> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, Priority.values());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPriority.setAdapter(adapter);

        // Manejar selección de fecha
        final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        final Date[] selectedDate = {null};

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar cal = Calendar.getInstance();
            cal.set(year, month, dayOfMonth);
            selectedDate[0] = cal.getTime();
        });

        builder.setPositiveButton("Filtrar", (dialog, which) -> {
            Priority prioridad = (Priority) spinnerPriority.getSelectedItem();
            String label = etLabel.getText().toString().trim();
            String fecha = selectedDate[0] != null ? sdf.format(selectedDate[0]) : "";

            filtrarTareas(
                    prioridad.equals(Priority.NONE) ? null : prioridad,
                    label,
                    fecha
            );
        });

        builder.setNegativeButton("Cancelar", null);
        builder.setNeutralButton("Limpiar fecha", (dialog, which) -> selectedDate[0] = null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }
    @Override
    protected void onPause() {
        super.onPause();
        saveTasks();
    }
    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
    // ---------------------------------------------------------------------------------------------
    // Método para programar la notificación 24h antes
    // ---------------------------------------------------------------------------------------------
    private void scheduleWorkReminder(Tarea tarea) {
        if (tarea.getFechaPlazo() == null) return;
        // Calcular tiempo para 24h antes de la fecha de plazo
        long triggerTime = tarea.getFechaPlazo().getTime() - 24 * 60 * 60 * 1000;
        long delay = triggerTime - System.currentTimeMillis();
        if (delay < 0) return; // Si ya pasó, no programamos

        int notificationId = (int) System.currentTimeMillis();
        Data inputData = new Data.Builder()
                .putString("tituloTarea", tarea.getTitulo())
                .putInt("notificationId", notificationId)
                .build();

        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(TaskReminderWorker.class)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .build();

        WorkManager.getInstance(this).enqueue(request);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        // Añadir esta línea si quieres un botón adicional en el menú
        menu.add(0, R.id.action_toggle_view, 0, "Cambiar vista");
        return true;
    }

    // Dentro de la clase MainActivity, después del onCreate()
    private void toggleViewMode() {
        if (recyclerTareas.getLayoutManager() instanceof CardStackLayoutManager) {
            recyclerTareas.setLayoutManager(new LinearLayoutManager(this));
        } else {
            recyclerTareas.setLayoutManager(new CardStackLayoutManager(this));
        }
        adaptador.notifyDataSetChanged();
    }

    // Dentro de la clase MainActivity
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_toggle_view) {
            toggleViewMode();
            return true;
        }
        // Maneja otros ítems del menú aquí...

        return super.onOptionsItemSelected(item);
    }
    // ---------------------------------------------------------------------------------------------
    // Diálogo para agregar tarea
    // ---------------------------------------------------------------------------------------------
    private void mostrarDialogoAgregarTarea() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_task, null);
        builder.setView(dialogView);

        EditText etTitulo = dialogView.findViewById(R.id.etTitulo);
        EditText etDescripcion = dialogView.findViewById(R.id.etDescripcion);
        Spinner spinnerPriority = dialogView.findViewById(R.id.spinnerPriority);
        EditText etLabel = dialogView.findViewById(R.id.etLabel);
        EditText etFechaPlazo = dialogView.findViewById(R.id.etFechaPlazo);

        ArrayAdapter<Priority> priorityAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, Priority.values());
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPriority.setAdapter(priorityAdapter);

        // Selección de color
        View viewColorRed = dialogView.findViewById(R.id.viewColorRed);
        View viewColorGreen = dialogView.findViewById(R.id.viewColorGreen);
        View viewColorBlue = dialogView.findViewById(R.id.viewColorBlue);
        final int[] chosenColor = {0xFF000000};

        viewColorRed.setOnClickListener(v -> chosenColor[0] = 0xFFFF0000);
        viewColorGreen.setOnClickListener(v -> chosenColor[0] = 0xFF00FF00);
        viewColorBlue.setOnClickListener(v -> chosenColor[0] = 0xFF0000FF);

        final Calendar plazoCalendar = Calendar.getInstance();
        etFechaPlazo.setOnClickListener(v -> {
            int year = plazoCalendar.get(Calendar.YEAR);
            int month = plazoCalendar.get(Calendar.MONTH);
            int day = plazoCalendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dpd = new DatePickerDialog(
                    MainActivity.this,
                    (view, year1, month1, dayOfMonth) -> {
                        plazoCalendar.set(Calendar.YEAR, year1);
                        plazoCalendar.set(Calendar.MONTH, month1);
                        plazoCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        etFechaPlazo.setText(sdf.format(plazoCalendar.getTime()));
                    },
                    year, month, day
            );
            dpd.show();
        });

        builder.setPositiveButton("Save", (dialog, which) -> {
            String titulo = etTitulo.getText().toString().trim();
            String descripcion = etDescripcion.getText().toString().trim();
            Priority selectedPriority = (Priority) spinnerPriority.getSelectedItem();
            String labelName = etLabel.getText().toString().trim();

            Tarea nuevaTarea = new Tarea(titulo, descripcion);
            nuevaTarea.setPrioridad(selectedPriority);
            nuevaTarea.setLabel(labelName);
            nuevaTarea.setLabelColor(chosenColor[0]);

            if (!etFechaPlazo.getText().toString().isEmpty()) {
                nuevaTarea.setFechaPlazo(plazoCalendar.getTime());
                scheduleWorkReminder(nuevaTarea);
            }

            listaTareas.add(nuevaTarea);
            allTasks.add(nuevaTarea);
            int newPosition = listaTareas.size() - 1;
            adaptador.notifyItemInserted(listaTareas.size() - 1);
            // Reproducir el sonido addedtask.mp3
            // Animación de superposición al añadir nueva tarjeta (Punto 4)
            recyclerTareas.post(() -> {
                if (newPosition >= 0 && newPosition < listaTareas.size()) {
                    recyclerTareas.smoothScrollToPosition(newPosition);

                    View newCard = recyclerTareas.getLayoutManager().findViewByPosition(newPosition);
                    if (newCard != null) {
                        // Configuración inicial para animación
                        newCard.setTranslationY(-100); // Desplazamiento inicial hacia arriba
                        newCard.setAlpha(0.5f); // Transparencia inicial
                        newCard.setZ(newPosition * 5); // Asegurar profundidad

                        // Animación de entrada
                        newCard.animate()
                                .translationY(0)
                                .alpha(1f)
                                .setDuration(300)
                                .setInterpolator(new OvershootInterpolator(1.2f))
                                .start();
                    }
                }
            });
            MediaPlayer mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.addedtask);
            mediaPlayer.start();
            // Liberar el MediaPlayer al finalizar
            mediaPlayer.setOnCompletionListener(mp -> {
                mp.release();
            });
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    // ---------------------------------------------------------------------------------------------
    // Diálogo para editar tarea
    // ---------------------------------------------------------------------------------------------
    private void mostrarDialogoEditarTarea(Tarea tarea, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_task, null);
        builder.setView(dialogView);

        EditText etTitulo = dialogView.findViewById(R.id.etTitulo);
        EditText etDescripcion = dialogView.findViewById(R.id.etDescripcion);
        Spinner spinnerPriority = dialogView.findViewById(R.id.spinnerPriority);
        EditText etLabel = dialogView.findViewById(R.id.etLabel);
        EditText etFechaPlazo = dialogView.findViewById(R.id.etFechaPlazo);

        etTitulo.setText(tarea.getTitulo());
        etDescripcion.setText(tarea.getDescripcion());
        etLabel.setText(tarea.getLabel());

        ArrayAdapter<Priority> priorityAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, Priority.values());
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPriority.setAdapter(priorityAdapter);
        spinnerPriority.setSelection(tarea.getPrioridad().ordinal());

        final int[] chosenColor = {tarea.getLabelColor()};
        View viewColorRed = dialogView.findViewById(R.id.viewColorRed);
        View viewColorGreen = dialogView.findViewById(R.id.viewColorGreen);
        View viewColorBlue = dialogView.findViewById(R.id.viewColorBlue);

        viewColorRed.setOnClickListener(v -> chosenColor[0] = 0xFFFF0000);
        viewColorGreen.setOnClickListener(v -> chosenColor[0] = 0xFF00FF00);
        viewColorBlue.setOnClickListener(v -> chosenColor[0] = 0xFF0000FF);

        // Pre-cargar fecha si existe
        final Calendar plazoCalendar = Calendar.getInstance();
        if (tarea.getFechaPlazo() != null) {
            plazoCalendar.setTime(tarea.getFechaPlazo());
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            etFechaPlazo.setText(sdf.format(tarea.getFechaPlazo()));
        }

        etFechaPlazo.setOnClickListener(v -> {
            int year = plazoCalendar.get(Calendar.YEAR);
            int month = plazoCalendar.get(Calendar.MONTH);
            int day = plazoCalendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dpd = new DatePickerDialog(MainActivity.this,
                    (view, year1, month1, dayOfMonth) -> {
                        plazoCalendar.set(Calendar.YEAR, year1);
                        plazoCalendar.set(Calendar.MONTH, month1);
                        plazoCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        etFechaPlazo.setText(sdf.format(plazoCalendar.getTime()));
                    },
                    year, month, day
            );
            dpd.show();
        });

        builder.setPositiveButton("Update", (dialog, which) -> {
            String titulo = etTitulo.getText().toString().trim();
            String descripcion = etDescripcion.getText().toString().trim();
            Priority selectedPriority = (Priority) spinnerPriority.getSelectedItem();
            String labelName = etLabel.getText().toString().trim();

            tarea.setTitulo(titulo);
            tarea.setDescripcion(descripcion);
            tarea.setPrioridad(selectedPriority);
            tarea.setLabel(labelName);
            tarea.setLabelColor(chosenColor[0]);

            // Actualizar fecha si se cambió
            if (!etFechaPlazo.getText().toString().isEmpty()) {
                tarea.setFechaPlazo(plazoCalendar.getTime());
                scheduleWorkReminder(tarea);
            }

            adaptador.notifyItemChanged(position);
            allTasks.set(position, tarea);
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    // ---------------------------------------------------------------------------------------------
    // Filtrado de tareas
    // ---------------------------------------------------------------------------------------------
    private void filtrarTareas(Priority prioridad, String label, String fechaStr) {
        List<Tarea> tareasFiltradas = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        for (Tarea t : allTasks) {
            boolean cumplePrioridad = (prioridad == null) || (t.getPrioridad() == prioridad);
            boolean cumpleLabel = label.isEmpty() || t.getLabel().equalsIgnoreCase(label);
            boolean cumpleFecha = true;

            if (!fechaStr.isEmpty()) {
                if (t.getFechaPlazo() == null) {
                    cumpleFecha = false;
                } else {
                    String fechaTarea = sdf.format(t.getFechaPlazo());
                    cumpleFecha = fechaTarea.equals(fechaStr);
                }
            }

            if (cumplePrioridad && cumpleLabel && cumpleFecha) {
                tareasFiltradas.add(t);
            }
        }

        listaTareas.clear();
        listaTareas.addAll(tareasFiltradas);
        adaptador.notifyDataSetChanged();

        String mensaje = tareasFiltradas.size() + " tareas encontradas";
        if (!fechaStr.isEmpty()) mensaje += "\nFecha: " + fechaStr;
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
    }

    // ---------------------------------------------------------------------------------------------
    // Ordenar tareas
    // ---------------------------------------------------------------------------------------------
    private void sortTasksByPriority() {
        Collections.sort(listaTareas, new Comparator<Tarea>() {
            @Override
            public int compare(Tarea t1, Tarea t2) {
                int val1 = priorityValue(t1.getPrioridad());
                int val2 = priorityValue(t2.getPrioridad());
                return Integer.compare(val2, val1);
            }
            private int priorityValue(Priority p) {
                switch (p) {
                    case HIGH:   return 3;
                    case MEDIUM: return 2;
                    case LOW:
                    default:     return 1;
                }
            }
        });
        adaptador.notifyDataSetChanged();
    }

    private void sortTasksByLabel() {
        Collections.sort(listaTareas, (t1, t2) ->
                t1.getLabel().compareToIgnoreCase(t2.getLabel()));
        adaptador.notifyDataSetChanged();
    }

    // Método para verificar y solicitar permiso
    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {

                // Explicación opcional (si es necesario)
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.POST_NOTIFICATIONS)) {
                    new AlertDialog.Builder(this)
                            .setTitle("Permiso requerido")
                            .setMessage("Este permiso es necesario para recordarte tus tareas")
                            .setPositiveButton("OK", (d, w) -> requestPermission())
                            .setNegativeButton("Cancelar", null)
                            .show();
                } else {
                    requestPermission();
                }
            }
        }
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.POST_NOTIFICATIONS},
                1001 // Código de solicitud
        );
    }
    // ---------------------------------------------------------------------------------------------
    // Swipe para eliminar con confirmación
    // ---------------------------------------------------------------------------------------------
    private void setupSwipeToDelete() {
        ItemTouchHelper.SimpleCallback simpleCallback =
                new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
                    @Override
                    public boolean onMove(RecyclerView recyclerView,
                                          RecyclerView.ViewHolder viewHolder,
                                          RecyclerView.ViewHolder target) {
                        return false;
                    }
                    @Override
                    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                        final int position = viewHolder.getAdapterPosition();
                        new AlertDialog.Builder(MainActivity.this)
                                .setMessage("¿Estás seguro de eliminar la tarea?")
                                .setPositiveButton("Sí", (dialog, which) -> {
                                    MediaPlayer mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.delete);
                                    mediaPlayer.start();
                                    // Liberar el MediaPlayer al finalizar
                                    mediaPlayer.setOnCompletionListener(mp -> {
                                        mp.release();
                                    });
                                    View itemView = recyclerTareas.findViewHolderForAdapterPosition(position).itemView;
                                    itemView.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.slide_out_right));
                                    itemView.postDelayed(() -> {
                                        listaTareas.remove(position);
                                        allTasks.remove(position);
                                        adaptador.notifyItemRemoved(position);
                                        saveTasks();
                                    }, 300);
                                })
                                .setNegativeButton("No", (dialog, which) -> {
                                    adaptador.notifyItemChanged(position);
                                })
                                .setCancelable(false)
                                .show();
                    }
                };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerTareas);
    }

    // ---------------------------------------------------------------------------------------------
    // Guardar / Cargar con SharedPreferences y Gson
    // ---------------------------------------------------------------------------------------------
    private void saveTasks() {
        SharedPreferences prefs = getSharedPreferences("TaskPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(listaTareas);
        editor.putString("tasks", json);
        editor.apply();
    }

    private void loadTasks() {
        SharedPreferences prefs = getSharedPreferences("TaskPrefs", MODE_PRIVATE);
        String json = prefs.getString("tasks", null);
        if (json != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<Tarea>>(){}.getType();
            listaTareas = gson.fromJson(json, type);
            if (listaTareas == null) {
                listaTareas = new ArrayList<>();
            }
        } else {
            listaTareas = new ArrayList<>();
        }
    }
}
