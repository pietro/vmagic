entity e is
end;

architecture a of e is
    signal s1, s2, s3 : bit;

begin
    process
    begin
        -- assertions
        assert cond;

        assert cond report "message";

        assert cond severity note;
        assert cond severity warning;
        assert cond severity error;
        assert cond severity failure;

        assert cond report "message" severity error;
        label_test_1 : assert cond report "message" severity error;

        -- case statements
        case expr is
            when '0' =>
                null;

            when '1' =>
                null;

            when '2' | '3' =>
                null;

            when others =>
                null;
        end case;

        label_test_2 : case expr is
            when '0' =>
                null;

            when '1' =>
                null;

            when '2' | '3' =>
                null;

            when others =>
                null;
        end case;

        -- if statements
        if cond then
            null;
        end if;

        if cond then
            null;
        elsif cond2 then
            null;
        end if;

        if cond then
            null;
        else
            null;
        end if;

        if cond then
            null;
        elsif cond2 then
            null;
        else
            null;
        end if;

        if cond then
            null;
        elsif cond2 then
            null;
        elsif cond3 then
            null;
        else
            null;
        end if;

        label_test_3 : if cond then
            null;
        elsif cond2 then
            null;
        elsif cond3 then
            null;
        else
            null;
        end if;

        -- loop/exit/next statements
        loop
            null;
        end loop;

        while expr loop
            null;
        end loop;

        for i in 1 to 100 loop
            null;
        end loop;

        label_test_4 : for i in 1 to 100 loop
                exit;
        	exit label_test_4;
        	exit when cond;
        	exit label_test_4 when cond;
                label_test_5 : exit label_test_4 when cond;

                next;
        	next label_test_4;
        	next when cond;
        	next label_test_4 when cond;
        	label_test_6 : next label_test_4 when cond;
        end loop;

        -- null statements
        null;
        label_test_7 : null;

        -- report statements
        report "message";

        report "message" severity note;
        report "message" severity warning;
        report "message" severity error;
        report "message" severity failure;
        label_test_8 : report "message" severity warning;

        -- return statements
        return;
    	return expr;
    	label_test_9: return expr;

        -- wait statements
        wait;
        wait on s1;
        wait on s1, s2, s3;
        wait until cond;
        wait for 10 ns;
        wait on s1 until cond;
        wait on s1, s2, s3 until cond;
        wait until cond for 10 ns;
        wait on s1, s2, s3 until cond for 10 ns;
        label_test_10 : wait on s1, s2, s3 until cond for 10 ns;
    end process;
end;