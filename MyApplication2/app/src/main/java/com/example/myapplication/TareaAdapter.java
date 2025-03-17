package com.example.myapplication;

import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
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
    private final int maxStackOffset;
    private final float rotationAngle;

    public TareaAdapter(List<Tarea> listaTareas, int maxStackOffsetDp, float rotationAngle) {
        this.listaTareas = listaTareas;
        this.maxStackOffset = maxStackOffsetDp;
        this.rotationAngle = rotationAngle;
    }

    public interface OnTaskClickListener {
        void onItemClick(Tarea tarea, int position);
        void onDetailsClick(Tarea tarea, int position);
    }

    public void setOnTaskClickListener(OnTaskClickListener listener) {
        this.listener = listener;
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
        holder.bind(listaTareas.get(position));
        applyStackEffect(holder.itemView, position);
    }

    private void applyStackEffect(View itemView, int position) {
        float density = itemView.getResources().getDisplayMetrics().density;
        int maxOffsetPx = (int) (maxStackOffset * density);

        // Aumentar el desplazamiento por cada tarjeta
        int offsetStep = maxOffsetPx / 3; // Más paso por posición

        // Reducir el máximo de desplazamiento para forzar superposición
        int calculatedOffsetX = Math.min(position * offsetStep, maxOffsetPx);
        int calculatedOffsetY = Math.min(position * (offsetStep * 2), (int)(maxOffsetPx * 1.5));

        // Aplicar superposición
        itemView.setTranslationX(calculatedOffsetX);
        itemView.setTranslationY(calculatedOffsetY);
        itemView.setRotation(position % 2 == 0 ? -rotationAngle : rotationAngle);
        itemView.setZ(position * 5); // Mayor diferencia de profundidad
    }

    @Override
    public int getItemCount() {
        return listaTareas.size();
    }

    class TareaViewHolder extends RecyclerView.ViewHolder {
        private final CheckBox checkCompleted;
        private final TextView tvTitulo, tvDescripcion;
        private final TextView tvPriorityValue, tvLabelValue, btnDetalles;
        private final View viewLabelColor;
        private final TextView tvDia, tvMes;
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
            btnDetalles = itemView.findViewById(R.id.btnDetalles);

            setupTouchInteractions();
            setupClickListeners();
            setupDetailsButton();
        }

        private void setupDetailsButton() {
            btnDetalles.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onDetailsClick(listaTareas.get(getAdapterPosition()), getAdapterPosition());
                }
            });
        }

        private void setupTouchInteractions() {
            itemView.setOnTouchListener((v, event) -> {
                Rect rect = new Rect();
                btnDetalles.getGlobalVisibleRect(rect);

                if (rect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    return false;
                }

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        animateCard(v, 12f, 1.03f);
                        return true;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        animateCard(v, 6f, 1f);
                        v.performClick();
                        return true;
                }
                return false;
            });
        }

        private void setupClickListeners() {
            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onItemClick(listaTareas.get(getAdapterPosition()), getAdapterPosition());
                }
            });

            checkCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (!isBinding && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listaTareas.get(getAdapterPosition()).setCompletada(isChecked);
                }
            });
        }

        private void animateCard(View view, float elevation, float scale) {
            view.animate()
                    .z(elevation)
                    .translationZ(elevation)
                    .scaleX(scale)
                    .scaleY(scale)
                    .setDuration(100)
                    .setInterpolator(new OvershootInterpolator(1.2f))
                    .start();
        }

        public void bind(Tarea tarea) {
            isBinding = true;
            setTaskData(tarea);
            setDateInfo(tarea);
            isBinding = false;
        }

        private void setTaskData(Tarea tarea) {
            checkCompleted.setChecked(tarea.isCompletada());
            tvTitulo.setText(tarea.getTitulo());
            tvDescripcion.setText(tarea.getDescripcion());
            tvPriorityValue.setText(tarea.getPrioridad().name());
            tvLabelValue.setText(tarea.getLabel());
            viewLabelColor.setBackgroundColor(tarea.getLabelColor());
        }

        private void setDateInfo(Tarea tarea) {
            if (tarea.getFechaPlazo() != null) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(tarea.getFechaPlazo());
                tvDia.setText(String.valueOf(cal.get(Calendar.DAY_OF_MONTH)));
                tvMes.setText(new SimpleDateFormat("MMM", Locale.getDefault())
                        .format(cal.getTime()).toUpperCase());
            } else {
                tvDia.setText("--");
                tvMes.setText("N/A");
            }
        }
    }
}