entity e is
end;

architecture a of e is
    -- enumeration types
    type BIT is ('0', '1');
    type STATE is (IDLE, RUNNING, STOPPED);
    type MIXED is ('0', A, '1', B, C);

    -- integer types
    type INT1 is range 0 to 255;
    type INT2 is range 1023 downto 0;
    subtype INT3 is INT1 range 0 to 127;

    -- physical types
    type MY_TIME is range 0 to 10e6
        units
            s;
            min = 60 s;
            h   = 60 min;
            d   = 24 h;
        end units;

    -- array types
    type ARRAY1 is array (NATURAL range <>) of BIT;
    type ARRAY2 is array (0 to 255) of BIT;

    -- record types
    type REC is
        record
            a : std_logic;
            b : std_logic_vector(7 downto 0);
        end record;

    -- access types
    type ARRAY1_POINTER is access ARRAY1;

    -- file types
    type FILE_TYPE is file of BIT;

    -- incomplete type
    type INCOMPLETE;

    -- subtype
    subtype ARRAY3 is ARRAY1(7 downto 0);

begin
end;