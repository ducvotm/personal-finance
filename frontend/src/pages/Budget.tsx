import { useEffect, useState } from 'react';
import { Link as RouterLink, useNavigate } from 'react-router-dom';
import {
  Box,
  Button,
  Card,
  CardBody,
  CardHeader,
  FormControl,
  FormLabel,
  Heading,
  HStack,
  Input,
  Link,
  Select,
  Stat,
  StatLabel,
  StatNumber,
  VStack,
  useToast,
  Progress,
  Text,
  Flex,
  IconButton,
  Modal,
  ModalOverlay,
  ModalContent,
  ModalHeader,
  ModalBody,
  ModalFooter,
  ModalCloseButton,
  useDisclosure,
  Grid,
  GridItem,
} from '@chakra-ui/react';
import { useBudgetStore } from '../stores/budgetStore';
import { useAuthStore } from '../stores/authStore';
import { categoryApi, Category } from '../api/client';

export default function BudgetPage() {
  const navigate = useNavigate();
  const logout = useAuthStore((s) => s.logout);
  const user = useAuthStore((s) => s.user);
  const { budgets, isLoading, fetchBudgets, createBudget, updateBudget, deleteBudget } = useBudgetStore();
  const [categories, setCategories] = useState<Category[]>([]);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [editingBudget, setEditingBudget] = useState<any>(null);
  const { isOpen, onOpen, onClose } = useDisclosure();
  const toast = useToast();

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  const today = new Date();
  const firstDayOfMonth = new Date(today.getFullYear(), today.getMonth(), 1);
  const lastDayOfMonth = new Date(today.getFullYear(), today.getMonth() + 1, 0);

  const [formData, setFormData] = useState({
    amount: 0,
    periodStart: firstDayOfMonth.toISOString().split('T')[0],
    periodEnd: lastDayOfMonth.toISOString().split('T')[0],
    periodType: 'MONTHLY',
    categoryId: 0,
  });

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    await fetchBudgets();
    try {
      const catRes = await categoryApi.getByType('EXPENSE');
      setCategories(catRes.data.data);
      if (catRes.data.data.length > 0) {
        setFormData(prev => ({ ...prev, categoryId: catRes.data.data[0].id }));
      }
    } catch (error) {
      console.error('Failed to load categories', error);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsSubmitting(true);

    try {
      if (editingBudget) {
        await updateBudget(editingBudget.id, formData);
        toast({ title: 'Budget updated', status: 'success', duration: 2000 });
      } else {
        await createBudget(formData);
        toast({ title: 'Budget created', status: 'success', duration: 2000 });
      }
      onClose();
      setEditingBudget(null);
      resetForm();
      loadData();
    } catch (error: any) {
      toast({ title: 'Error', description: error.message, status: 'error', duration: 3000 });
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleDelete = async (id: number) => {
    if (!confirm('Are you sure you want to delete this budget?')) return;
    try {
      await deleteBudget(id);
      toast({ title: 'Budget deleted', status: 'success', duration: 2000 });
    } catch (error: any) {
      toast({ title: 'Error', description: error.message, status: 'error', duration: 3000 });
    }
  };

  const handleEdit = (budget: any) => {
    setEditingBudget(budget);
    setFormData({
      amount: budget.amount,
      periodStart: budget.periodStart,
      periodEnd: budget.periodEnd,
      periodType: budget.periodType,
      categoryId: budget.categoryId,
    });
    onOpen();
  };

  const resetForm = () => {
    const firstDay = new Date(today.getFullYear(), today.getMonth(), 1);
    const lastDay = new Date(today.getFullYear(), today.getMonth() + 1, 0);
    setFormData({
      amount: 0,
      periodStart: firstDay.toISOString().split('T')[0],
      periodEnd: lastDay.toISOString().split('T')[0],
      periodType: 'MONTHLY',
      categoryId: categories.length > 0 ? categories[0].id : 0,
    });
  };

  const openNewBudgetModal = () => {
    setEditingBudget(null);
    resetForm();
    onOpen();
  };

  const getProgressColor = (percent: number) => {
    if (percent >= 100) return 'red';
    if (percent >= 80) return 'orange';
    return 'green';
  };

  return (
    <Box minH="100vh" bg="gray.50" p={6}>
      <Flex justify="space-between" align="center" mb={6} wrap="wrap" gap={3}>
        <HStack spacing={4} flexWrap="wrap">
          <Link as={RouterLink} to="/" color="blue.600" fontWeight="medium">
            Home
          </Link>
          <Heading size="lg">Category budgets</Heading>
        </HStack>
        <HStack>
          <Text fontSize="sm" color="gray.600">
            {user?.username}
          </Text>
          <Button colorScheme="blue" onClick={openNewBudgetModal}>
            Add budget
          </Button>
          <Button size="sm" variant="outline" onClick={handleLogout}>
            Logout
          </Button>
        </HStack>
      </Flex>

      {isLoading ? (
        <Text>Loading...</Text>
      ) : budgets.length === 0 ? (
        <Card>
          <CardBody>
            <Text color="gray.500" textAlign="center">
              No budgets yet. Create your first budget to track spending!
            </Text>
          </CardBody>
        </Card>
      ) : (
        <Grid templateColumns={{ base: '1fr', md: 'repeat(2, 1fr)', lg: 'repeat(3, 1fr)' }} gap={4}>
          {budgets.map((budget) => (
            <GridItem key={budget.id}>
              <Card>
                <CardHeader pb={2}>
                  <HStack justify="space-between">
                    <Box>
                      <Heading size="sm">{budget.categoryName}</Heading>
                      <Text fontSize="xs" color="gray.500">
                        {budget.periodType} ({budget.periodStart} - {budget.periodEnd})
                      </Text>
                    </Box>
                    <HStack>
                      <IconButton
                        aria-label="Edit"
                        icon={<Text>✏️</Text>}
                        size="sm"
                        variant="ghost"
                        onClick={() => handleEdit(budget)}
                      />
                      <IconButton
                        aria-label="Delete"
                        icon={<Text>🗑️</Text>}
                        size="sm"
                        variant="ghost"
                        colorScheme="red"
                        onClick={() => handleDelete(budget.id)}
                      />
                    </HStack>
                  </HStack>
                </CardHeader>
                <CardBody pt={0}>
                  <Stat>
                    <StatLabel>Budget</StatLabel>
                    <StatNumber>${budget.amount.toFixed(2)}</StatNumber>
                  </Stat>
                  <Box mt={2}>
                    <HStack justify="space-between" fontSize="sm" mb={1}>
                      <Text color="gray.600">Spent: ${budget.spentAmount.toFixed(2)}</Text>
                      <Text color="gray.600">Remaining: ${budget.remainingAmount.toFixed(2)}</Text>
                    </HStack>
                    <Progress
                      value={Math.min(budget.percentUsed, 100)}
                      colorScheme={getProgressColor(budget.percentUsed)}
                      size="sm"
                      borderRadius="full"
                    />
                    <Text fontSize="xs" color="gray.500" mt={1} textAlign="right">
                      {budget.percentUsed.toFixed(1)}% used
                    </Text>
                  </Box>
                </CardBody>
              </Card>
            </GridItem>
          ))}
        </Grid>
      )}

      <Modal isOpen={isOpen} onClose={onClose} size="md">
        <ModalOverlay />
        <ModalContent>
          <ModalHeader>{editingBudget ? 'Edit Budget' : 'Create Budget'}</ModalHeader>
          <ModalCloseButton />
          <form onSubmit={handleSubmit}>
            <ModalBody>
              <VStack spacing={4}>
                <FormControl isRequired>
                  <FormLabel>Category</FormLabel>
                  <Select
                    value={formData.categoryId}
                    onChange={(e) => setFormData({ ...formData, categoryId: parseInt(e.target.value) })}
                    isDisabled={!!editingBudget}
                  >
                    {categories.map((cat) => (
                      <option key={cat.id} value={cat.id}>
                        {cat.name}
                      </option>
                    ))}
                  </Select>
                </FormControl>

                <FormControl isRequired>
                  <FormLabel>Budget Amount</FormLabel>
                  <Input
                    type="number"
                    step="0.01"
                    min="0.01"
                    value={formData.amount || ''}
                    onChange={(e) => setFormData({ ...formData, amount: parseFloat(e.target.value) || 0 })}
                    placeholder="0.00"
                  />
                </FormControl>

                <HStack spacing={4} w="full">
                  <FormControl isRequired>
                    <FormLabel>Start Date</FormLabel>
                    <Input
                      type="date"
                      value={formData.periodStart}
                      onChange={(e) => setFormData({ ...formData, periodStart: e.target.value })}
                    />
                  </FormControl>
                  <FormControl isRequired>
                    <FormLabel>End Date</FormLabel>
                    <Input
                      type="date"
                      value={formData.periodEnd}
                      onChange={(e) => setFormData({ ...formData, periodEnd: e.target.value })}
                    />
                  </FormControl>
                </HStack>
              </VStack>
            </ModalBody>
            <ModalFooter>
              <Button variant="ghost" mr={3} onClick={onClose}>
                Cancel
              </Button>
              <Button type="submit" colorScheme="blue" isLoading={isSubmitting}>
                {editingBudget ? 'Update' : 'Create'}
              </Button>
            </ModalFooter>
          </form>
        </ModalContent>
      </Modal>
    </Box>
  );
}