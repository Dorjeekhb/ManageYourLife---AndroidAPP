package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class TareaAdapter extends RecyclerView.Adapter<TareaAdapter.TareaViewHolder> {

    private List<Tarea> listaTareas;
    private OnTaskClickListener listener;

    // Interfaz para comunicar clics al Activity (para editar la tarea)
    public interface OnTaskClickListener {
        void onItemClick(Tarea tarea, int position);
    }

    public void setOnTaskClickListener(OnTaskClickListener listener) {
        this.listener = listener;
    }

    public TareaAdapter(List<Tarea> listaTareas) {
        this.listaTareas = listaTareas;
    }

    @NonNull
    @Override
    public TareaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tarea, parent, false);
        return new TareaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TareaViewHolder holder, int position) {
        Tarea tareaActual = listaTareas.get(position);
        holder.bind(tareaActual);
    }

    @Override
    public int getItemCount() {
        return listaTareas.size();
    }

    class TareaViewHolder extends RecyclerView.ViewHolder {
        private CheckBox checkCompleted;
        private TextView tvTitulo, tvDescripcion;
        private TextView tvPriorityValue, tvLabelValue;
        private View viewLabelColor;

        // Para el mini-calendario
        private TextView tvDia, tvMes;

        private boolean isBinding;

        public TareaViewHolder(@NonNull View itemView) {
            super(itemView);
            checkCompleted = itemView.findViewById(R.id.checkCompleted);
            tvTitulo = itemView.findViewById(R.id.tvTitulo);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcion);
            tvPriorityValue = itemView.findViewById(R.id.tvPriorityValue);
            tvLabelValue = itemView.findViewById(R.id.tvLabelValue);
            viewLabelColor = itemView.findViewById(R.id.viewLabelColor);

            tvDia = itemView.findViewById(R.id.tvDia);
            tvMes = itemView.findViewById(R.id.tvMes);

            // Listener para detectar clic en el ítem y notificar al Activity
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int pos = getAdapterPosition();
                        if (pos != RecyclerView.NO_POSITION) {
                            listener.onItemClick(listaTareas.get(pos), pos);
                        }
                    }
                }
            });

            // Listener para el CheckBox: al marcar, se pueden sumar puntos, etc.
            checkCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (!isBinding) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Tarea tarea = listaTareas.get(position);
                        boolean wasCompleted = tarea.isCompletada();
                        tarea.setCompletada(isChecked);
                        // Aquí puedes agregar lógica para sumar puntos, etc.
                    }
                }
            });
        }

        public void bind(Tarea tarea) {
            isBinding = true;
            checkCompleted.setChecked(tarea.isCompletada());
            tvTitulo.setText(tarea.getTitulo());
            tvDescripcion.setText(tarea.getDescripcion());
            tvPriorityValue.setText(tarea.getPrioridad().name());
            tvLabelValue.setText(tarea.getLabel());
            viewLabelColor.setBackgroundColor(tarea.getLabelColor());

            // Mostrar la fecha de plazo en formato de mini-calendario
            if (tarea.getFechaPlazo() != null) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(tarea.getFechaPlazo());
                int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);

                tvDia.setText(String.valueOf(dayOfMonth));

                // Convertir el número de mes a abreviatura (por ejemplo, "ENE")
                SimpleDateFormat sdfMonth = new SimpleDateFormat("MMM", Locale.getDefault());
                String monthAbbrev = sdfMonth.format(cal.getTime()).toUpperCase();
                tvMes.setText(monthAbbrev);
            } else {
                tvDia.setText("--");
                tvMes.setText("N/A");
            }
            isBinding = false;
        }
    }
}
