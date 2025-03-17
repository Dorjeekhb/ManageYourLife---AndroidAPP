package com.example.myapplication;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class TaskReminderWorker extends Worker {

    public TaskReminderWorker(@NonNull android.content.Context context,
                              @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        // 1. Obtener datos
        String tituloTarea = getInputData().getString("tituloTarea");
        int notificationId = getInputData().getInt("notificationId", 0);

        // 2. Crear el canal de notificaciones (Android O+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Tareas Channel";
            String description = "Canal para recordatorios de tareas";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("task_channel", name, importance);
            channel.setDescription(description);

            NotificationManager manager =
                    getApplicationContext().getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        // 3. Construir la notificación
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(getApplicationContext(), "task_channel")
                        .setSmallIcon(R.drawable.ic_launcher_foreground) // Usa tu propio ícono
                        .setContentTitle("Tarea próxima a vencer")
                        .setContentText("Recuerda: " + tituloTarea + " vence pronto.")
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true);

        // 4. Verificar permiso POST_NOTIFICATIONS (Android 13+)
        if (ActivityCompat.checkSelfPermission(
                getApplicationContext(),
                android.Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED) {
            // Si no hay permiso, se omite la notificación
            return Result.success();
        }

        // 5. Enviar la notificación
        NotificationManagerCompat.from(getApplicationContext())
                .notify(notificationId, builder.build());

        return Result.success();
    }
}
