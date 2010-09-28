use std.textio.all;

entity ent is
end;

architecture beh of ent is
    -- subprogram declarations
    procedure procedure1;
    procedure procedure2(constant c1, c2 : bit := '1';
                         signal s1, s2 : out bit;
                         variable v1, v2 : bit;
                         file f1, f2 : text
                        );

    function function1 return bit;
    pure function function2 return bit;
    impure function function3 return bit;
    function function4(constant c1, c2 : bit := '1';
                       signal s1, s2 : bit;
                       file f1, f2 : text
                      ) return bit;

    -- subprogram bodies
    procedure procedure1 is
    begin
    end;

    procedure procedure2(constant c1, c2 : bit := '1';
                         signal s1, s2 : out bit;
                         variable v1, v2 : bit;
                         file f1, f2 : text
                        ) is
    begin
    end;

    function function1 return bit is
    begin
    end;

    pure function function2 return bit is
    begin
    end;

    impure function function3 return bit is
    begin
    end;

    function function4 (constant c1, c2 : bit := '1';
                        signal s1, s2 : bit;
                        file f1, f2 : text
                       ) return bit is
    begin
    end;

    function function5(i : integer) return integer is
        constant a  : integer := 5;
        variable v1 : integer;
        variable v2 : bit_vector(a - 1 downto 0);
    begin
        v1 := i;
        v1 := v1 + a;
        return v1;
    end;

begin

	process
		variable l : line;
	begin
		write(l, function5(1));
		writeline(output, l);
		wait;
	end process;
end;