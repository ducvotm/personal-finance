import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Box,
  Button,
  Card,
  CardBody,
  CardHeader,
  Divider,
  FormControl,
  FormLabel,
  Heading,
  HStack,
  Input,
  Select,
  Stat,
  StatArrow,
  StatHelpText,
  StatLabel,
  StatNumber,
  Table,
  Tbody,
  Td,
  Text,
  Th,
  Thead,
  Tr,
  VStack,
  useToast,
  IconButton,
  Flex,
} from '@chakra-ui/react';
import { useAuthStore } from '../stores/authStore';
import { accountApi, categoryApi, transactionApi, Transaction, TransactionRequest } from '../api/client';

export default function Dashboard() {
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [summary, setSummary] = useState({ totalIncome: 0, totalExpense: 0, netBalance: 0 });
  const [accounts, setAccounts] = useState<{ id: number; name: string }[]>([]);
  const [categories, setCategories] = useState<{ id: number; name: string; type: string }[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);
  
  const [formData, setFormData] = useState<TransactionRequest>({
    amount: 0,
    type: 'EXPENSE',
    transactionDate: new Date().toISOString().split('T')[0],
    description: '',
    accountId: 0,
    categoryId: 0,
  });

  const navigate = useNavigate();
  const toast = useToast();
  const logout = useAuthStore((state) => state.logout);
  const user = useAuthStore((state) => state.user);

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    setIsLoading(true);
    try {
      const [transRes, accRes, catRes] = await Promise.all([
        transactionApi.getAll(0, 10),
        accountApi.getAll(),
        categoryApi.getAll(),
      ]);

      setTransactions(transRes.data.data.content);
      setAccounts(accRes.data.data);

      const today = new Date();
      const firstDay = new Date(today.getFullYear(), today.getMonth(), 1);
      const summaryRes = await transactionApi.getSummary(
        firstDay.toISOString().split('T')[0],
        today.toISOString().split('T')[0]
      );
      setSummary(summaryRes.data.data);

      const allCategories = catRes.data.data;
      setCategories(allCategories);

      if (accRes.data.data.length > 0) {
        setFormData(prev => ({ ...prev, accountId: accRes.data.data[0].id }));
      }
      if (allCategories.length > 0) {
        const filteredCats = allCategories.filter((c: any) => c.type === 'EXPENSE');
        if (filteredCats.length > 0) {
          setFormData(prev => ({ ...prev, categoryId: filteredCats[0].id }));
        }
      }
    } catch (error: any) {
      toast({
        title: 'Error loading data',
        description: error.message,
        status: 'error',
        duration: 3000,
      });
    } finally {
      setIsLoading(false);
    }
  };

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsSubmitting(true);

    try {
      await transactionApi.create(formData);
      toast({
        title: 'Transaction created',
        status: 'success',
        duration: 2000,
      });
      setFormData({
        ...formData,
        amount: 0,
        description: '',
        transactionDate: new Date().toISOString().split('T')[0],
      });
      loadData();
    } catch (error: any) {
      toast({
        title: 'Error creating transaction',
        description: error.response?.data?.message || error.message,
        status: 'error',
        duration: 3000,
      });
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleTypeChange = (type: 'INCOME' | 'EXPENSE') => {
    setFormData(prev => ({ ...prev, type }));
    const filteredCats = categories.filter(c => c.type === type);
    if (filteredCats.length > 0) {
      setFormData(prev => ({ ...prev, categoryId: filteredCats[0].id }));
    }
  };

  const filteredCategories = categories.filter(c => c.type === formData.type);

  return (
    <Box minH="100vh" bg="gray.50" p={6}>
      <Flex justify="space-between" align="center" mb={6}>
        <Heading size="lg">Personal Finance</Heading>
        <HStack>
          <Text>Welcome, {user?.username}</Text>
          <Button colorScheme="red" size="sm" onClick={handleLogout}>
            Logout
          </Button>
        </HStack>
      </Flex>

      <HStack spacing={4} mb={6} wrap="wrap">
        <Card flex="1" minW="200px">
          <CardBody>
            <Stat>
              <StatLabel>Income (This Month)</StatLabel>
              <StatNumber color="green.500">
                ${summary.totalIncome.toFixed(2)}
              </StatNumber>
            </Stat>
          </CardBody>
        </Card>
        <Card flex="1" minW="200px">
          <CardBody>
            <Stat>
              <StatLabel>Expense (This Month)</StatLabel>
              <StatNumber color="red.500">
                ${summary.totalExpense.toFixed(2)}
              </StatNumber>
            </Stat>
          </CardBody>
        </Card>
        <Card flex="1" minW="200px">
          <CardBody>
            <Stat>
              <StatLabel>Net Balance</StatLabel>
              <StatNumber color={summary.netBalance >= 0 ? 'green.500' : 'red.500'}>
                ${summary.netBalance.toFixed(2)}
              </StatNumber>
              <StatHelpText>
                <StatArrow type={summary.netBalance >= 0 ? 'increase' : 'decrease'} />
                This month
              </StatHelpText>
            </Stat>
          </CardBody>
        </Card>
      </HStack>

      <HStack align="start" spacing={6} wrap="wrap">
        <Card flex="1" minW="300px">
          <CardHeader>
            <Heading size="md">Add Transaction</Heading>
          </CardHeader>
          <CardBody>
            <form onSubmit={handleSubmit}>
              <VStack spacing={4}>
                <FormControl isRequired>
                  <FormLabel>Type</FormLabel>
                  <Select
                    value={formData.type}
                    onChange={(e) => handleTypeChange(e.target.value as 'INCOME' | 'EXPENSE')}
                  >
                    <option value="EXPENSE">Expense</option>
                    <option value="INCOME">Income</option>
                  </Select>
                </FormControl>

                <FormControl isRequired>
                  <FormLabel>Amount</FormLabel>
                  <Input
                    type="number"
                    step="0.01"
                    min="0.01"
                    value={formData.amount || ''}
                    onChange={(e) => setFormData({ ...formData, amount: parseFloat(e.target.value) || 0 })}
                    placeholder="0.00"
                  />
                </FormControl>

                <FormControl isRequired>
                  <FormLabel>Account</FormLabel>
                  <Select
                    value={formData.accountId}
                    onChange={(e) => setFormData({ ...formData, accountId: parseInt(e.target.value) })}
                  >
                    {accounts.map((acc) => (
                      <option key={acc.id} value={acc.id}>
                        {acc.name}
                      </option>
                    ))}
                  </Select>
                </FormControl>

                <FormControl isRequired>
                  <FormLabel>Category</FormLabel>
                  <Select
                    value={formData.categoryId}
                    onChange={(e) => setFormData({ ...formData, categoryId: parseInt(e.target.value) })}
                  >
                    {filteredCategories.map((cat) => (
                      <option key={cat.id} value={cat.id}>
                        {cat.name}
                      </option>
                    ))}
                  </Select>
                </FormControl>

                <FormControl isRequired>
                  <FormLabel>Date</FormLabel>
                  <Input
                    type="date"
                    value={formData.transactionDate}
                    onChange={(e) => setFormData({ ...formData, transactionDate: e.target.value })}
                  />
                </FormControl>

                <FormControl>
                  <FormLabel>Description</FormLabel>
                  <Input
                    value={formData.description}
                    onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                    placeholder="Enter description"
                  />
                </FormControl>

                <Button
                  type="submit"
                  colorScheme="blue"
                  width="full"
                  isLoading={isSubmitting}
                >
                  Add Transaction
                </Button>
              </VStack>
            </form>
          </CardBody>
        </Card>

        <Card flex="2" minW="400px">
          <CardHeader>
            <Heading size="md">Recent Transactions</Heading>
          </CardHeader>
          <CardBody>
            {isLoading ? (
              <Text>Loading...</Text>
            ) : transactions.length === 0 ? (
              <Text color="gray.500">No transactions yet</Text>
            ) : (
              <Box overflowX="auto">
                <Table size="sm">
                  <Thead>
                    <Tr>
                      <Th>Date</Th>
                      <Th>Description</Th>
                      <Th>Category</Th>
                      <Th isNumeric>Amount</Th>
                    </Tr>
                  </Thead>
                  <Tbody>
                    {transactions.map((trans) => (
                      <Tr key={trans.id}>
                        <Td>{trans.transactionDate}</Td>
                        <Td>{trans.description || '-'}</Td>
                        <Td>{trans.categoryName}</Td>
                        <Td isNumeric color={trans.type === 'INCOME' ? 'green.500' : 'red.500'}>
                          {trans.type === 'INCOME' ? '+' : '-'}${trans.amount.toFixed(2)}
                        </Td>
                      </Tr>
                    ))}
                  </Tbody>
                </Table>
              </Box>
            )}
          </CardBody>
        </Card>
      </HStack>
    </Box>
  );
}
