import sys
import joblib
import os
import pandas as pd

# Nombres de columnas correctos según el entrenamiento del modelo
columnas = [
    'Pregnancies', 'Glucose', 'BloodPressure', 'SkinThickness',
    'Insulin', 'BMI', 'DiabetesPedigreeFunction', 'Age'
]

# Obtener rutas del modelo y del scaler
ruta_actual = os.path.dirname(os.path.abspath(__file__))
ruta_modelo = os.path.join(ruta_actual, 'modelo_diabetes_decision_tree.pkl')
ruta_scaler = os.path.join(ruta_actual, 'scaler_diabetes.pkl')

# Leer los parámetros desde sys.argv (omitimos el nombre del script)
datos = list(map(float, sys.argv[1:]))

# Crear DataFrame con nombres de columnas para evitar el warning de scikit-learn
df_datos = pd.DataFrame([datos], columns=columnas)

# Cargar modelo y scaler
modelo = joblib.load(ruta_modelo)
scaler = joblib.load(ruta_scaler)

# Escalar los datos
datos_escalados = scaler.transform(df_datos)

# Realizar predicción
resultado = modelo.predict(datos_escalados)
probabilidad = modelo.predict_proba(datos_escalados)[0][1] * 100

# Imprimir resultado
if resultado[0] == 1:
    print(f"Riesgo de Diabetes: Positivo \nse le recomienda Registrar una cita para Medicina general")
else:
    print(f"Riesgo de Diabetes: Negativo")
