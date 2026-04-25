package com.keyboardunity

object HidDescriptor {

    val COMBINED_DESCRIPTOR: ByteArray = byteArrayOf(
        // ── Keyboard (Report ID 1) ─────────────────────────────────────────
        0x05.b, 0x01.b,  // Usage Page (Generic Desktop)
        0x09.b, 0x06.b,  // Usage (Keyboard)
        0xA1.b, 0x01.b,  // Collection (Application)
        0x85.b, 0x01.b,  //   Report ID (1)
        // Modifier byte
        0x05.b, 0x07.b,  //   Usage Page (Key Codes)
        0x19.b, 0xE0.b,  //   Usage Minimum (Left Ctrl)
        0x29.b, 0xE7.b,  //   Usage Maximum (Right GUI)
        0x15.b, 0x00.b,  //   Logical Minimum (0)
        0x25.b, 0x01.b,  //   Logical Maximum (1)
        0x75.b, 0x01.b,  //   Report Size (1 bit)
        0x95.b, 0x08.b,  //   Report Count (8)
        0x81.b, 0x02.b,  //   Input (Data, Variable, Absolute)
        // Reserved byte
        0x95.b, 0x01.b,  //   Report Count (1)
        0x75.b, 0x08.b,  //   Report Size (8 bits)
        0x81.b, 0x03.b,  //   Input (Const)
        // LED output (5 bits + 3 padding)
        0x95.b, 0x05.b,  //   Report Count (5)
        0x75.b, 0x01.b,  //   Report Size (1 bit)
        0x05.b, 0x08.b,  //   Usage Page (LEDs)
        0x19.b, 0x01.b,  //   Usage Minimum (Num Lock)
        0x29.b, 0x05.b,  //   Usage Maximum (Kana)
        0x91.b, 0x02.b,  //   Output (Data, Variable, Absolute)
        0x95.b, 0x01.b,  //   Report Count (1)
        0x75.b, 0x03.b,  //   Report Size (3 bits)
        0x91.b, 0x03.b,  //   Output (Const)
        // Key array (6 keys)
        0x95.b, 0x06.b,  //   Report Count (6)
        0x75.b, 0x08.b,  //   Report Size (8 bits)
        0x15.b, 0x00.b,  //   Logical Minimum (0)
        0x25.b, 0x65.b,  //   Logical Maximum (101)
        0x05.b, 0x07.b,  //   Usage Page (Key Codes)
        0x19.b, 0x00.b,  //   Usage Minimum (0)
        0x29.b, 0x65.b,  //   Usage Maximum (101)
        0x81.b, 0x00.b,  //   Input (Data, Array, Absolute)
        0xC0.b,          // End Collection

        // ── Mouse (Report ID 2) ────────────────────────────────────────────
        0x05.b, 0x01.b,  // Usage Page (Generic Desktop)
        0x09.b, 0x02.b,  // Usage (Mouse)
        0xA1.b, 0x01.b,  // Collection (Application)
        0x85.b, 0x02.b,  //   Report ID (2)
        0x09.b, 0x01.b,  //   Usage (Pointer)
        0xA1.b, 0x00.b,  //   Collection (Physical)
        // Buttons (3 bits + 5 padding)
        0x05.b, 0x09.b,  //     Usage Page (Buttons)
        0x19.b, 0x01.b,  //     Usage Minimum (Button 1)
        0x29.b, 0x03.b,  //     Usage Maximum (Button 3)
        0x15.b, 0x00.b,  //     Logical Minimum (0)
        0x25.b, 0x01.b,  //     Logical Maximum (1)
        0x75.b, 0x01.b,  //     Report Size (1 bit)
        0x95.b, 0x03.b,  //     Report Count (3)
        0x81.b, 0x02.b,  //     Input (Data, Variable, Absolute)
        0x75.b, 0x05.b,  //     Report Size (5 bits)
        0x95.b, 0x01.b,  //     Report Count (1)
        0x81.b, 0x03.b,  //     Input (Const) – button padding
        // X, Y, Wheel
        0x05.b, 0x01.b,  //     Usage Page (Generic Desktop)
        0x09.b, 0x30.b,  //     Usage (X)
        0x09.b, 0x31.b,  //     Usage (Y)
        0x09.b, 0x38.b,  //     Usage (Wheel)
        0x15.b, 0x81.b,  //     Logical Minimum (-127)
        0x25.b, 0x7F.b,  //     Logical Maximum (127)
        0x75.b, 0x08.b,  //     Report Size (8 bits)
        0x95.b, 0x03.b,  //     Report Count (3)
        0x81.b, 0x06.b,  //     Input (Data, Variable, Relative)
        0xC0.b,          //   End Collection
        0xC0.b           // End Collection
    )

    // ── Keyboard HID key codes ─────────────────────────────────────────────
    const val KEY_NONE          = 0x00
    const val KEY_A             = 0x04
    const val KEY_B             = 0x05
    const val KEY_C             = 0x06
    const val KEY_D             = 0x07
    const val KEY_E             = 0x08
    const val KEY_F             = 0x09
    const val KEY_G             = 0x0A
    const val KEY_H             = 0x0B
    const val KEY_I             = 0x0C
    const val KEY_J             = 0x0D
    const val KEY_K             = 0x0E
    const val KEY_L             = 0x0F
    const val KEY_M             = 0x10
    const val KEY_N             = 0x11
    const val KEY_O             = 0x12
    const val KEY_P             = 0x13
    const val KEY_Q             = 0x14
    const val KEY_R             = 0x15
    const val KEY_S             = 0x16
    const val KEY_T             = 0x17
    const val KEY_U             = 0x18
    const val KEY_V             = 0x19
    const val KEY_W             = 0x1A
    const val KEY_X             = 0x1B
    const val KEY_Y             = 0x1C
    const val KEY_Z             = 0x1D
    const val KEY_1             = 0x1E
    const val KEY_2             = 0x1F
    const val KEY_3             = 0x20
    const val KEY_4             = 0x21
    const val KEY_5             = 0x22
    const val KEY_6             = 0x23
    const val KEY_7             = 0x24
    const val KEY_8             = 0x25
    const val KEY_9             = 0x26
    const val KEY_0             = 0x27
    const val KEY_ENTER         = 0x28
    const val KEY_ESCAPE        = 0x29
    const val KEY_BACKSPACE     = 0x2A
    const val KEY_TAB           = 0x2B
    const val KEY_SPACE         = 0x2C
    const val KEY_MINUS         = 0x2D
    const val KEY_EQUAL         = 0x2E
    const val KEY_LEFT_BRACKET  = 0x2F
    const val KEY_RIGHT_BRACKET = 0x30
    const val KEY_BACKSLASH     = 0x31
    const val KEY_SEMICOLON     = 0x33
    const val KEY_APOSTROPHE    = 0x34
    const val KEY_GRAVE         = 0x35
    const val KEY_COMMA         = 0x36
    const val KEY_DOT           = 0x37
    const val KEY_SLASH         = 0x38
    const val KEY_CAPS_LOCK     = 0x39
    const val KEY_F1            = 0x3A
    const val KEY_F2            = 0x3B
    const val KEY_F3            = 0x3C
    const val KEY_F4            = 0x3D
    const val KEY_F5            = 0x3E
    const val KEY_F6            = 0x3F
    const val KEY_F7            = 0x40
    const val KEY_F8            = 0x41
    const val KEY_F9            = 0x42
    const val KEY_F10           = 0x43
    const val KEY_F11           = 0x44
    const val KEY_F12           = 0x45
    const val KEY_INSERT        = 0x49
    const val KEY_HOME          = 0x4A
    const val KEY_PAGE_UP       = 0x4B
    const val KEY_DELETE        = 0x4C
    const val KEY_END           = 0x4D
    const val KEY_PAGE_DOWN     = 0x4E
    const val KEY_RIGHT         = 0x4F
    const val KEY_LEFT          = 0x50
    const val KEY_DOWN          = 0x51
    const val KEY_UP            = 0x52

    // ── Modifier bits ──────────────────────────────────────────────────────
    const val MOD_LEFT_CTRL  = 0x01
    const val MOD_LEFT_SHIFT = 0x02
    const val MOD_LEFT_ALT   = 0x04
    const val MOD_LEFT_GUI   = 0x08
    const val MOD_RIGHT_CTRL = 0x10
    const val MOD_RIGHT_SHIFT = 0x20
    const val MOD_RIGHT_ALT  = 0x40
    const val MOD_RIGHT_GUI  = 0x80

    // ── Report IDs ─────────────────────────────────────────────────────────
    const val REPORT_ID_KEYBOARD = 1
    const val REPORT_ID_MOUSE    = 2

    // Extension to make byte array literals less noisy
    private val Int.b: Byte get() = toByte()
}
