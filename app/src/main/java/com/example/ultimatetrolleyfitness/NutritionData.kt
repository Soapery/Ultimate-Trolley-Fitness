import android.content.Context
import android.util.Log
import com.opencsv.CSVReader
import java.io.IOException
import java.io.InputStreamReader

object NutritionData {

    fun readNutrientsCSV(context: Context) {
        val filename = "nutrients_csvfile.csv" // Replace with your actual CSV file name
        try {
            val inputStream = context.assets.open(filename)
            val inputStreamReader = InputStreamReader(inputStream)
            val csvReader = CSVReader(inputStreamReader)
            var nextLine: Array<String>?
            while (csvReader.readNext().also { nextLine = it } != null) {
                // Process each line from the CSV file
                // For example, to display in logs:
                Log.d("CSV Data", nextLine.contentToString())
                // Or perform other operations with the data
            }
            csvReader.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}