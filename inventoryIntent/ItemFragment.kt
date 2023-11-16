package com.bignerdranch.android.inventoryIntent

import android.Manifest
import android.app.Activity
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TimePicker
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import java.text.SimpleDateFormat
import java.util.*


private const val ARG_ITEM_ID = "item_id"
private const val DIALOG_DATE = "DialogDate"
private const val REQUEST_DATE = 0
private const val REQUEST_CONTACT = 1
private const val REQUEST_PHONE = 2
private const val DATE_FORMAT = "EEE, MM, dd"


class ItemFragment : Fragment(), DatePickerFragment.Callbacks {
    private lateinit var item: Item
    //details
    private lateinit var modelField: EditText

    //item
    private lateinit var itemAmount: EditText
    private lateinit var increaseButton: Button
    private lateinit var decreaseButton: Button

    //date and time picker
    private lateinit var datePickerButton: Button
    private lateinit var timePickerButton: Button

    //inventory report and shipping status
    private lateinit var inShippingCheckBox: CheckBox
    private lateinit var inventoryReportButton: Button
    private lateinit var supplierButton: Button
    private lateinit var callButton: Button

    //scan code
    private lateinit var imageView: ImageView
    private lateinit var radioGroup: RadioGroup
    private lateinit var rb1: RadioButton
    private lateinit var rb2: RadioButton



    private val itemDetailViewModel: ItemDetailViewModel by lazy {
        ViewModelProviders.of(this).get(ItemDetailViewModel::class.java)
    }



    companion object {
        fun newInstance(itemId: UUID): ItemFragment {
            val args = Bundle().apply {
                putSerializable(ARG_ITEM_ID, itemId)
            }
            return ItemFragment().apply {
                arguments = args
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        item = Item()
        val itemId: UUID = arguments?.getSerializable(ARG_ITEM_ID) as UUID
        itemDetailViewModel.loadItem(itemId)
    }



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_item, container, false)
        modelField = view.findViewById(R.id.item_model) as EditText

        itemAmount = view.findViewById(R.id.itemAmountInput) as EditText
        increaseButton = view.findViewById(R.id.increase) as Button
        decreaseButton = view.findViewById(R.id.decrease) as Button

        increaseButton.setOnClickListener {
            //convert itemAmount to int
            val currentAmountInt = item.itemAmount.toInt()
            val newAmountInt = currentAmountInt + 1
            //convert back to string
            val newAmountString = newAmountInt.toString()

            // Update the itemAmount property in the item object with the new value
            item.itemAmount = newAmountString

            // Update the EditText to display the new value
            itemAmount.setText(newAmountString)
        }

        decreaseButton.setOnClickListener {
            //convert itemAmount to int
            val currentAmountInt = item.itemAmount.toInt()

            var newAmountInt = currentAmountInt - 1
            //if amount decreases lower than 0, set item amount as 0
            if (newAmountInt <= 0) {
                newAmountInt = 0
            }

            //convert back to string
            val newAmountString = newAmountInt.toString()

            // Update the itemAmount property in the item object with the new value
            item.itemAmount = newAmountString

            // Update the EditText to display the new value
            itemAmount.setText(newAmountString)
        }


        inShippingCheckBox = view.findViewById(R.id.item_shipment) as CheckBox
        inventoryReportButton = view.findViewById(R.id.item_report) as Button
        supplierButton = view.findViewById(R.id.item_supplier) as Button
        callButton = view.findViewById(R.id.supplier_call) as Button

        imageView = view.findViewById(R.id.ItemImageView) as ImageView

        datePickerButton = view.findViewById(R.id.item_date) as Button
        if (item.date == null ){
            datePickerButton.text="SELECT DATE"
        }

        timePickerButton = view.findViewById(R.id.timePickerButton) as Button
        if (item.time.isEmpty()){
            timePickerButton.text="SELECT TIME"
        }


        radioGroup = view.findViewById(R.id.radio_group) as RadioGroup
        rb1 = view.findViewById(R.id.radio_button_option1) as RadioButton
        rb2 = view.findViewById(R.id.radio_button_option2) as RadioButton

        radioGroup.check(R.id.radio_button_option1) // This will select "Option 1" by default
        imageView.setImageResource(R.drawable.ic_shortcut_paymentqrcode)


        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radio_button_option1 -> {
                    imageView.setImageResource(R.drawable.ic_shortcut_paymentqrcode)
                }

                R.id.radio_button_option2 -> {
                    imageView.setImageResource(R.drawable.ic_shortcut_attendanceqrcode)
                }
            }


        }

        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        itemDetailViewModel.itemLiveData.observe(
            viewLifecycleOwner
        ) { item ->
            item?.let {
                this.item = item
                updateUI()
            }
        }

        datePickerButton.isEnabled = item.inShipment
        timePickerButton.isEnabled = item.inShipment
    }



    private fun updateUI() {
        modelField.setText(item.model)
        itemAmount.setText(item.itemAmount)

        if (item.time.isNotEmpty()) {
            timePickerButton.text=item.time
        }

        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        datePickerButton.text = dateFormat.format(item.date)

        inShippingCheckBox.apply {
            isChecked = item.inShipment
            jumpDrawablesToCurrentState()
        }

        if (item.supplier.isNotEmpty()) {
            supplierButton.text = item.supplier
        }
    }



    private fun getInventoryReport(): String {
        val solvedString = if (item.inShipment) {
            getString(R.string.item_report_inShipment)
        } else {
            getString(R.string.item_report_replenished)
        }
        val dateString = DateFormat.format(DATE_FORMAT, item.date).toString()

        val supplierName = getString(R.string.item_report_supplier, item.supplier)

        return getString(R.string.item_report, item.model, dateString,item.time, solvedString, supplierName)
    }



    override fun onStart() {
        super.onStart()
        val titleWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // This space intentionally left blank
            }

            override fun onTextChanged(
                sequence: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                item.model = sequence.toString()
            }

            override fun afterTextChanged(s: Editable?) {
                // This space intentionally left blank
            }
        }

        modelField.addTextChangedListener(titleWatcher)

        //newly added: for item amount
        val amountWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // This space intentionally left blank
            }

            override fun onTextChanged(
                sequence: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                item.itemAmount = sequence.toString()
            }

            override fun afterTextChanged(s: Editable?) {
                // This space intentionally left blank
            }
        }

        itemAmount.addTextChangedListener(amountWatcher)


        inShippingCheckBox.apply {
            setOnCheckedChangeListener { _, isChecked ->
                item.inShipment = isChecked
                datePickerButton.isEnabled = isChecked
                timePickerButton.isEnabled = isChecked}
        }

        datePickerButton.setOnClickListener {
            DatePickerFragment.newInstance(item.date).apply {
                setTargetFragment(this@ItemFragment, REQUEST_DATE)
                show(this@ItemFragment.requireFragmentManager(), DIALOG_DATE)
            }
        }



        inventoryReportButton.setOnClickListener {
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, getInventoryReport())
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.item_report_subject))
            }.also { intent ->
                val chooserIntent = Intent.createChooser(intent, getString(R.string.send_report))
                startActivity(chooserIntent)
            }
        }

        supplierButton.apply {
            val pickContactIntent =
                Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
            setOnClickListener {
                startActivityForResult(pickContactIntent, REQUEST_CONTACT)
            }

            val packageManager: PackageManager = requireActivity().packageManager
            val resolvedActivity: ResolveInfo? =
                packageManager.resolveActivity(pickContactIntent, PackageManager.MATCH_DEFAULT_ONLY)

            Log.d("SupplierButton", resolvedActivity.toString())
            if (resolvedActivity == null) {
                isEnabled = false
            }
        }

        callButton.apply {

            when (PackageManager.PERMISSION_GRANTED) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_CONTACTS
                ) -> {
                    val pickPhoneIntent =
                        Intent(
                            Intent.ACTION_PICK,
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI
                        )
                    setOnClickListener {
                        startActivityForResult(pickPhoneIntent, REQUEST_PHONE)
                    }
                }
                else -> {
                    requestPermissions(
                        arrayOf(Manifest.permission.READ_CONTACTS),
                        REQUEST_PHONE
                    )
                }
            }
        }


        timePickerButton.setOnClickListener {
            // Get the current time from the calendar
            val calendar = Calendar.getInstance()
            val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            // Create a TimePickerDialog to show the time picker
            val timePickerDialog = TimePickerDialog(
                requireContext(),
                TimePickerDialog.OnTimeSetListener { view: TimePicker, hourOfDay: Int, minute: Int ->
                    // Update the text of the button with the selected time
                    val timeText = String.format("%02d:%02d", hourOfDay, minute)
                    item.time= timeText
                    timePickerButton.text=item.time

                },
                hourOfDay,
                minute,
                true
            )

            // Show the time picker dialog
            timePickerDialog.show()
        }


    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)


        when (requestCode) {
            REQUEST_PHONE -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED)
                ) {
                    val pickPhoneIntent =
                        Intent(
                            Intent.ACTION_PICK
                        ).apply {
                            type = ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE
                        }
                    startActivityForResult(pickPhoneIntent, REQUEST_PHONE)

                } else {
                    Log.e("ItemFragment", "Unavailable permissions CONTACTS")
                }
                return
            }



            else -> {
            }
        }
    }

    override fun onStop() {
        super.onStop()
        itemDetailViewModel.saveItem(item)
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)


        when (requestCode){

            REQUEST_CONTACT -> {
                if (resultCode == Activity.RESULT_OK && data != null) {

                    val contactUri: Uri? = data.data
                    // Specify which fields you want your query to return values for
                    val queryFields = arrayOf(ContactsContract.Contacts.DISPLAY_NAME)
                    // Perform your query - the contactUri is like a "where" clause here
                    val cursor = requireActivity().contentResolver
                        .query(contactUri!!, queryFields, null, null, null)
                    cursor?.use {
                        // Verify that the cursor contains at least one result
                        if (it.count == 0) {
                            return
                        }
                        // Pull out the first column of the first row of data -
                        // that is your supplier's name
                        it.moveToFirst()
                        val supplier = it.getString(0)
                        item.supplier = supplier
                        itemDetailViewModel.saveItem(item)
                        supplierButton.text = supplier
                    }
                }
            }


            REQUEST_PHONE -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    Log.i("Request Phone", "URI: ${data.data}")

                    val contactUri = data.data
                    val queries = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    val cursor = requireActivity()
                        .contentResolver
                        .query(
                            contactUri!!,
                            queries,
                            null,
                            null,
                            null
                        )

                    cursor?.use { it ->
                        if (it.count == 0) {
                            return
                        }

                        it.moveToFirst()

                        val phoneIndex =
                            cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                        Log.d("Phone index", phoneIndex.toString())

                        val phone = it.getString(phoneIndex)

                        Log.d("SupplierPhone", phone)
                        val number: Uri = Uri.parse("tel:$phone")

                        startActivity(Intent(Intent.ACTION_DIAL, number))

                    }

                }
            }

        }
    }

    override fun onDateSelected(date: Date) {
        item.date = date
        updateUI()
    }



}